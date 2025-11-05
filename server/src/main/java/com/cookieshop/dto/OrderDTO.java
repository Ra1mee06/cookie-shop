package com.cookieshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO {
    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items = new ArrayList<>();
    private String recipient;
    private String address;
    private String comment;
    private String promoCode;
    private String paymentMethod;
    private BigDecimal tip;
    private BigDecimal discount;
    
    public OrderDTO() {}
    
    public OrderDTO(Long id, Long userId, BigDecimal totalPrice, String status, 
                    LocalDateTime createdAt, LocalDateTime updatedAt, List<OrderItemDTO> items) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }
    
    // геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<OrderItemDTO> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getPromoCode() {
        return promoCode;
    }
    
    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public BigDecimal getTip() {
        return tip;
    }
    
    public void setTip(BigDecimal tip) {
        this.tip = tip;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}

