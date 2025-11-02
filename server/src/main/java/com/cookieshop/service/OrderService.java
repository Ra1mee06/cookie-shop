package com.cookieshop.service;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.dto.OrderItemDTO;
import com.cookieshop.dto.ProductDTO;
import com.cookieshop.entity.Order;
import com.cookieshop.entity.OrderItem;
import com.cookieshop.entity.Product;
import com.cookieshop.entity.User;
import com.cookieshop.repository.OrderRepository;
import com.cookieshop.repository.ProductRepository;
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
    
    @Transactional
    public OrderDTO createOrder(Map<String, Object> orderData, Long userId) {
        try {
            // Получаем или создаем пользователя
            User user = null;
            if (userId != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                }
            }
            
            // Если userId не найден, используем первого доступного пользователя (для тестирования)
            if (user == null) {
                List<User> users = userRepository.findAll();
                if (!users.isEmpty()) {
                    user = users.get(0);
                }
            }
            
            // Создаем заказ
            Order order = new Order();
            order.setUser(user);
            
            // Получаем totalPrice
            Object totalPriceObj = orderData.get("totalPrice");
            BigDecimal totalPrice;
            if (totalPriceObj instanceof Number) {
                totalPrice = BigDecimal.valueOf(((Number) totalPriceObj).doubleValue());
            } else {
                totalPrice = new BigDecimal(totalPriceObj.toString());
            }
            order.setTotalPrice(totalPrice);
            order.setStatus(Order.OrderStatus.PENDING);
            
            // Сохраняем заказ сначала, чтобы получить ID
            order = orderRepository.save(order);
            
            // Обрабатываем items
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) orderData.get("items");
            if (itemsData != null) {
                for (Map<String, Object> itemData : itemsData) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    
                    // Получаем productId
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
                    orderItem.setProduct(productOpt.get());
                    
                    // Получаем quantity
                    Object quantityObj = itemData.get("quantity");
                    Integer quantity;
                    if (quantityObj instanceof Number) {
                        quantity = ((Number) quantityObj).intValue();
                    } else {
                        quantity = Integer.parseInt(quantityObj.toString());
                    }
                    orderItem.setQuantity(quantity);
                    
                    // Получаем price
                    Object priceObj = itemData.get("price");
                    BigDecimal price;
                    if (priceObj instanceof Number) {
                        price = BigDecimal.valueOf(((Number) priceObj).doubleValue());
                    } else {
                        price = new BigDecimal(priceObj.toString());
                    }
                    orderItem.setPrice(price);
                    
                    order.getItems().add(orderItem);
                }
            }
            
            // Сохраняем заказ со всеми items
            order = orderRepository.save(order);
            
            // Перезагружаем для гарантии загрузки всех связей
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
    
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
        }
        
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Конвертируем items
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

