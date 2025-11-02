package com.cookieshop.dto;

public class FavoriteDTO {
    private Long id;
    private Long userId;
    private ProductDTO product;
    
    public FavoriteDTO() {}
    
    public FavoriteDTO(Long id, Long userId, ProductDTO product) {
        this.id = id;
        this.userId = userId;
        this.product = product;
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
    
    public ProductDTO getProduct() {
        return product;
    }
    
    public void setProduct(ProductDTO product) {
        this.product = product;
    }
}

