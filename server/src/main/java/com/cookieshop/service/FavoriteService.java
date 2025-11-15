package com.cookieshop.service;

import com.cookieshop.dto.FavoriteDTO;
import com.cookieshop.dto.ProductDTO;
import com.cookieshop.entity.Favorite;
import com.cookieshop.entity.Product;
import com.cookieshop.entity.User;
import com.cookieshop.repository.FavoriteRepository;
import com.cookieshop.repository.ProductRepository;
import com.cookieshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<FavoriteDTO> getAllFavoritesByUserId(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public FavoriteDTO addFavorite(Long userId, Long productId) {
        try {
            // Проверка существования
            Optional<Favorite> existing = favoriteRepository.findByUserIdAndProductId(userId, productId);
            if (existing.isPresent()) {
                return convertToDTO(existing.get());
            }
            
            // Проверка пользователя
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                List<User> allUsers = userRepository.findAll();
                String availableIds = allUsers.stream()
                    .map(u -> u.getId().toString())
                    .collect(Collectors.joining(", "));
                throw new RuntimeException("User with id " + userId + " not found. Available users: " + availableIds);
            }
            User user = userOpt.get();
            
            // Проверка продукта
            Optional<Product> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                List<Product> allProducts = productRepository.findAll();
                String availableIds = allProducts.stream()
                    .map(p -> p.getId().toString())
                    .collect(Collectors.joining(", "));
                throw new RuntimeException("Product with id " + productId + " not found. Available products: " + availableIds);
            }
            Product product = productOpt.get();
            
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setProduct(product);
            
            Favorite saved = favoriteRepository.save(favorite);
            
            // Добавление в БД
            Favorite reloaded = favoriteRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload favorite after save"));
            
            return convertToDTO(reloaded);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error adding favorite: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public void removeFavorite(Long favoriteId) {
        favoriteRepository.deleteById(favoriteId);
    }
    
    @Transactional
    public void removeFavoriteByProductId(Long userId, Long productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    @Transactional
    public void clearAllFavorites(Long userId) {
        favoriteRepository.deleteByUserId(userId);
    }
    
    private FavoriteDTO convertToDTO(Favorite favorite) {
        if (favorite == null) {
            throw new RuntimeException("Favorite is null");
        }
        
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        
        User user = favorite.getUser();
        if (user == null) {
            throw new RuntimeException("User is null in favorite with id " + favorite.getId());
        }
        dto.setUserId(user.getId());
        
        Product product = favorite.getProduct();
        if (product == null) {
            throw new RuntimeException("Product is null in favorite with id " + favorite.getId());
        }
        
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setTitle(product.getTitle());
        productDTO.setPrice(product.getPrice());
        productDTO.setImageUrl(product.getImageUrl());
        productDTO.setDescription(product.getDescription());
        productDTO.setIngredients(product.getIngredients());
        productDTO.setCalories(product.getCalories());
        productDTO.setStory(product.getStory());
        
        dto.setProduct(productDTO);
        return dto;
    }
}

