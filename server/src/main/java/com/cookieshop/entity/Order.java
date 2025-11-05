package com.cookieshop.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(name = "recipient")
    private String recipient;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "promo_code")
    private String promoCode;
    
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    
    @Column(name = "tip")
    private BigDecimal tip = BigDecimal.ZERO;
    
    @Column(name = "discount")
    private BigDecimal discount = BigDecimal.ZERO;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, DELIVERED, CANCELLED
    }
    
    public enum PaymentMethod {
        CASH, CARD_ONLINE, CARD_ON_DELIVERY
    }
    
    // геттеры и сеттеры
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
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
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
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

