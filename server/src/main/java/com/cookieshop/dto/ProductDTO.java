// ProductDTO.java
package com.cookieshop.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String imageUrl;
    private String description;
    private String ingredients;
    private String calories;
    private String story;
    
    // конструкторы
    public ProductDTO() {}
    
    public ProductDTO(Long id, String title, BigDecimal price, String imageUrl) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    
    // геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    
    public String getCalories() { return calories; }
    public void setCalories(String calories) { this.calories = calories; }
    
    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }
}