package com.cookieshop.entity;

import javax.persistence.*;

@Entity
@Table(name = "suggestions")
public class Suggestion extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "author", nullable = false)
    private String author;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    // геттеры и сеттеры
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

