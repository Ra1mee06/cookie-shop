// ProductService.java
package com.cookieshop.service;

import com.cookieshop.dto.ProductDTO;
import com.cookieshop.entity.Product;
import com.cookieshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<ProductDTO> getAllProducts(String search, String sortBy, String sortOrder) {
        List<Product> products;
        
        // Фильтрация по поисковому запросу
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.findByTitleContainingIgnoreCase(search.trim());
        } else {
            products = productRepository.findAll();
        }
        
        // Сортировка
        if (sortBy != null && sortBy.equals("price")) {
            if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                // Сортировка по убыванию цены
                products = products.stream()
                        .sorted((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()))
                        .collect(Collectors.toList());
            } else {
                // Сортировка по возрастанию цены
                products = products.stream()
                        .sorted((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()))
                        .collect(Collectors.toList());
            }
        } else {
            // Сортировка по названию (по умолчанию)
            products = products.stream()
                    .sorted((p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()))
                    .collect(Collectors.toList());
        }
        
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDTO(product);
    }
    
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setDescription(product.getDescription());
        dto.setIngredients(product.getIngredients());
        dto.setCalories(product.getCalories());
        dto.setStory(product.getStory());
        return dto;
    }
    
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setDescription(dto.getDescription());
        product.setIngredients(dto.getIngredients());
        product.setCalories(dto.getCalories());
        product.setStory(dto.getStory());
        return product;
    }
}