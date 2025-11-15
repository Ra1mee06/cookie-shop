package com.cookieshop.service;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.dto.OrderItemDTO;
import com.cookieshop.dto.ProductDTO;
import com.cookieshop.entity.Order;
import com.cookieshop.entity.OrderItem;
import com.cookieshop.entity.Product;
import com.cookieshop.entity.PromoCode;
import com.cookieshop.entity.User;
import com.cookieshop.repository.OrderRepository;
import com.cookieshop.repository.ProductRepository;
import com.cookieshop.repository.PromoCodeRepository;
import com.cookieshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private PromoCodeRepository promoCodeRepository;
    
    @Transactional
    public OrderDTO createOrder(Map<String, Object> orderData, Long userId) {
        try {
            // Получение пользователя
            User user = null;
            if (userId != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                }
            }
            
            if (user == null) {
                List<User> users = userRepository.findAll();
                if (!users.isEmpty()) {
                    user = users.get(0);
                }
            }
            
            // Создание заказа
            Order order = new Order();
            order.setUser(user);
            
            Object totalPriceObj = orderData.get("totalPrice");
            BigDecimal originalTotalPrice;
            if (totalPriceObj instanceof Number) {
                originalTotalPrice = BigDecimal.valueOf(((Number) totalPriceObj).doubleValue());
            } else {
                originalTotalPrice = new BigDecimal(totalPriceObj.toString());
            }
            // Обработка промокода
            BigDecimal discount = BigDecimal.ZERO;
            String promoCode = null;
            PromoCode promoEntityForOrder = null;
            if (orderData.containsKey("promoCode") && orderData.get("promoCode") != null) {
                promoCode = orderData.get("promoCode").toString().trim().toUpperCase();
                if (promoCode.isEmpty()) {
                    promoCode = null;
                } else {
                    // Проверка промокода
                    PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(promoCode).orElse(null);
                    if (promo == null) {
                        throw new RuntimeException("Промокод не найден или неверный. Пожалуйста, проверьте введенный промокод.");
                    }
                    
                    int used = (promo.getUsedCount() != null) ? promo.getUsedCount() : 0;
                    if (!promo.isActive()) {
                        throw new RuntimeException("Промокод неактивен. Пожалуйста, проверьте введенный промокод.");
                    }
                    if (promo.getExpiresAt() != null && promo.getExpiresAt().isBefore(java.time.OffsetDateTime.now())) {
                        throw new RuntimeException("Промокод истек. Пожалуйста, проверьте введенный промокод.");
                    }
                    if (promo.getMaxUses() != null && used >= promo.getMaxUses()) {
                        throw new RuntimeException("Промокод больше не может быть использован. Пожалуйста, проверьте введенный промокод.");
                    }
                    
                    if (!orderData.containsKey("discount")) {
                        promoEntityForOrder = promo;
                        switch (promo.getType()) {
                            case ORDER_PERCENT:
                                if (promo.getValue() != null) {
                                    discount = originalTotalPrice.multiply(promo.getValue()).divide(new BigDecimal("100"));
                                }
                                break;
                            case GIFT_CERTIFICATE:
                                if (promo.getValue() != null) {
                                    discount = promo.getValue();
                                    if (discount.compareTo(originalTotalPrice) > 0) {
                                        discount = originalTotalPrice;
                                    }
                                }
                                break;
                            case PRODUCT_PERCENT:
                                promoEntityForOrder = promo;
                                break;
                            case BUY2GET1:
                                promoEntityForOrder = promo;
                                break;
                        }
                    } else {
                        promoEntityForOrder = promo;
                        Object discountObj = orderData.get("discount");
                        if (discountObj instanceof Number) {
                            discount = BigDecimal.valueOf(((Number) discountObj).doubleValue());
                        } else if (discountObj != null) {
                            discount = new BigDecimal(discountObj.toString());
                        }
                    }
                }
            }
            
            // Обработка промокода "2 по цене 1"
            if (promoCode != null && promoCode.equals("BUY2GET1")) {
            }
            
            BigDecimal finalTotal;
            if (orderData.containsKey("finalTotal")) {
                Object finalTotalObj = orderData.get("finalTotal");
                if (finalTotalObj instanceof Number) {
                    finalTotal = BigDecimal.valueOf(((Number) finalTotalObj).doubleValue());
                } else {
                    finalTotal = new BigDecimal(finalTotalObj.toString());
                }
            } else {
                finalTotal = originalTotalPrice.subtract(discount);
            }
            
            order.setTotalPrice(finalTotal);
            order.setDiscount(discount);
            order.setPromoCode(promoCode);
            order.setStatus(Order.OrderStatus.PENDING);
            
            if (orderData.containsKey("recipient")) {
                order.setRecipient(orderData.get("recipient").toString());
            }
            if (orderData.containsKey("address")) {
                order.setAddress(orderData.get("address").toString());
            }
            if (orderData.containsKey("comment")) {
                Object commentObj = orderData.get("comment");
                if (commentObj != null) {
                    order.setComment(commentObj.toString());
                }
            }
            if (orderData.containsKey("paymentMethod")) {
                String paymentMethodStr = orderData.get("paymentMethod").toString();
                try {
                    order.setPaymentMethod(Order.PaymentMethod.valueOf(paymentMethodStr));
                } catch (IllegalArgumentException e) {
                    order.setPaymentMethod(Order.PaymentMethod.CASH);
                }
            }
            if (orderData.containsKey("tip")) {
                Object tipObj = orderData.get("tip");
                if (tipObj instanceof Number) {
                    order.setTip(BigDecimal.valueOf(((Number) tipObj).doubleValue()));
                } else if (tipObj != null) {
                    order.setTip(new BigDecimal(tipObj.toString()));
                }
            }
            
            // Добавление в БД
            order = orderRepository.save(order);
            
            // Обработка товаров
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) orderData.get("items");
            if (itemsData != null) {
                boolean applyBuy2Get1 = false;
                PromoCode promoEntity = promoEntityForOrder != null ? promoEntityForOrder : (promoCode != null ? promoCodeRepository.findByCodeIgnoreCase(promoCode).orElse(null) : null);
                if (promoEntity != null && promoEntity.getType() == PromoCode.PromoType.BUY2GET1) {
                    applyBuy2Get1 = true;
                }
                
                for (Map<String, Object> itemData : itemsData) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    
                    Object productIdObj = itemData.get("productId");
                    Long productId;
                    if (productIdObj instanceof Number) {
                        productId = ((Number) productIdObj).longValue();
                    } else {
                        productId = Long.parseLong(productIdObj.toString());
                    }
                    
                    Optional<Product> productOpt = productRepository.findById(productId);
                    if (!productOpt.isPresent()) {
                        throw new RuntimeException("Product with id " + productId + " not found");
                    }
                    Product product = productOpt.get();
                    orderItem.setProduct(product);
                    
                    Object quantityObj = itemData.get("quantity");
                    Integer quantity;
                    if (quantityObj instanceof Number) {
                        quantity = ((Number) quantityObj).intValue();
                    } else {
                        quantity = Integer.parseInt(quantityObj.toString());
                    }
                    
                    // Применение промокода "2 по цене 1"
                    if (applyBuy2Get1 && quantity >= 2) {
                        int paidQuantity = (quantity + 1) / 2;
                        orderItem.setQuantity(quantity);
                        BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(paidQuantity));
                        orderItem.setPrice(itemPrice);
                    } else {
                        orderItem.setQuantity(quantity);
                        
                        Object priceObj = itemData.get("price");
                        BigDecimal price;
                        if (priceObj instanceof Number) {
                            price = BigDecimal.valueOf(((Number) priceObj).doubleValue());
                        } else {
                            price = new BigDecimal(priceObj.toString());
                        }
                        if (promoEntity != null && promoEntity.getType() == PromoCode.PromoType.PRODUCT_PERCENT
                                && promoEntity.getProduct() != null && promoEntity.getValue() != null
                                && product.getId().equals(promoEntity.getProduct().getId())) {
                            BigDecimal factor = BigDecimal.ONE.subtract(promoEntity.getValue().divide(new BigDecimal("100")));
                            if (factor.compareTo(BigDecimal.ZERO) < 0) factor = BigDecimal.ZERO;
                            price = price.multiply(factor);
                        }
                        orderItem.setPrice(price);
                    }
                    
                    order.getItems().add(orderItem);
                }
                
                // Пересчет суммы при промокоде "2 по цене 1"
                if (applyBuy2Get1) {
                    BigDecimal recalculatedTotal = BigDecimal.ZERO;
                    for (OrderItem item : order.getItems()) {
                        recalculatedTotal = recalculatedTotal.add(item.getPrice());
                    }
                    BigDecimal totalWithTip = recalculatedTotal.add(order.getTip());
                    order.setTotalPrice(totalWithTip);
                    discount = originalTotalPrice.subtract(recalculatedTotal);
                    if (discount.compareTo(BigDecimal.ZERO) < 0) {
                        discount = BigDecimal.ZERO;
                    }
                    order.setDiscount(discount);
                }
            }
            
            // Сохранение заказа
            order = orderRepository.save(order);
            if (promoEntityForOrder != null) {
                Integer usedCount = promoEntityForOrder.getUsedCount();
                if (usedCount == null) usedCount = 0;
                promoEntityForOrder.setUsedCount(usedCount + 1);
                promoCodeRepository.save(promoEntityForOrder);
            }
            
            Order reloaded = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload order after save"));
            
            return convertToDTO(reloaded);
        } catch (Exception e) {
            throw new RuntimeException("Error creating order: " + e.getMessage(), e);
        }
    }
    
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public OrderDTO getOrderById(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order with id " + orderId + " not found");
        }
        return convertToDTO(orderOpt.get());
    }

    public List<OrderDTO> listAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO adminUpdateOrder(Long orderId, Map<String, Object> updates) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order with id " + orderId + " not found"));
        if (updates.containsKey("recipient")) {
            Object v = updates.get("recipient");
            order.setRecipient(v == null ? null : v.toString());
        }
        if (updates.containsKey("address")) {
            Object v = updates.get("address");
            order.setAddress(v == null ? null : v.toString());
        }
        if (updates.containsKey("comment")) {
            Object v = updates.get("comment");
            order.setComment(v == null ? null : v.toString());
        }
        if (updates.containsKey("paymentMethod")) {
            Object v = updates.get("paymentMethod");
            if (v != null) {
                try {
                    order.setPaymentMethod(Order.PaymentMethod.valueOf(v.toString()));
                } catch (IllegalArgumentException ignore) { }
            }
        }
        if (updates.containsKey("status")) {
            Object v = updates.get("status");
            if (v != null) {
                try {
                    order.setStatus(Order.OrderStatus.valueOf(v.toString()));
                } catch (IllegalArgumentException ignore) { }
            }
        }
        if (updates.containsKey("totalPrice")) {
            Object v = updates.get("totalPrice");
            if (v instanceof Number) {
                order.setTotalPrice(new BigDecimal(((Number) v).toString()));
            } else if (v != null) {
                order.setTotalPrice(new BigDecimal(v.toString()));
            }
        }
        if (updates.containsKey("discount")) {
            Object v = updates.get("discount");
            if (v instanceof Number) {
                order.setDiscount(new BigDecimal(((Number) v).toString()));
            } else if (v != null) {
                order.setDiscount(new BigDecimal(v.toString()));
            }
        }
        order = orderRepository.save(order);
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO changeStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order with id " + orderId + " not found"));
        order.setStatus(Order.OrderStatus.valueOf(status));
        order = orderRepository.save(order);
        return convertToDTO(order);
    }
    
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        
        if (order.getUser() != null) {
            User user = order.getUser();
            dto.setUserId(user.getId());
            dto.setUserEmail(user.getEmail());
            dto.setUserUsername(user.getUsername());
            dto.setUserFullName(user.getFullName());
        }
        
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        dto.setRecipient(order.getRecipient());
        dto.setAddress(order.getAddress());
        dto.setComment(order.getComment());
        dto.setPromoCode(order.getPromoCode());
        if (order.getPaymentMethod() != null) {
            dto.setPaymentMethod(order.getPaymentMethod().name());
        }
        dto.setTip(order.getTip());
        dto.setDiscount(order.getDiscount());
        
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);
        
        return dto;
    }
    
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(item.getProduct().getId());
            productDTO.setTitle(item.getProduct().getTitle());
            productDTO.setPrice(item.getProduct().getPrice());
            productDTO.setImageUrl(item.getProduct().getImageUrl());
            productDTO.setDescription(item.getProduct().getDescription());
            productDTO.setIngredients(item.getProduct().getIngredients());
            productDTO.setCalories(item.getProduct().getCalories());
            productDTO.setStory(item.getProduct().getStory());
            
            dto.setProduct(productDTO);
        }
        
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        
        return dto;
    }
}

