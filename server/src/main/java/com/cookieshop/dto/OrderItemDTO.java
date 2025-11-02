package com.cookieshop.dto;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long id;
    private Long productId;
    private ProductDTO product;
    private Integer quantity;
    private BigDecimal price;
    
    public OrderItemDTO() {}
    
    public OrderItemDTO(Long id, Long productId, ProductDTO product, Integer quantity, BigDecimal price) {
        this.id = id;
        this.productId = productId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }
    
    // геттеры и сеттеры
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public ProductDTO getProduct() {
        return product;
    }
    
    public void setProduct(ProductDTO product) {
        this.product = product;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

