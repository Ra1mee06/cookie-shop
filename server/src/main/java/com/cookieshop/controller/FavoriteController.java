package com.cookieshop.controller;

import com.cookieshop.dto.FavoriteDTO;
import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import com.cookieshop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getAllFavorites(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<FavoriteDTO> favorites = favoriteService.getAllFavoritesByUserId(userId);
        return ResponseEntity.ok(favorites);
    }
    
    @PostMapping
    public ResponseEntity<?> addFavorite(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        try {
            if (userId == null) {
                List<User> users = userRepository.findAll();
                if (users.isEmpty()) {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "No users found in database. Please create a user first.");
                    return ResponseEntity.status(500).body(response);
                }
                userId = users.get(0).getId(); // Используем первого доступного пользователя
            } else {
                // Проверка пользователя
                if (!userRepository.findById(userId).isPresent()) {
                    List<User> users = userRepository.findAll();
                    if (users.isEmpty()) {
                        Map<String, String> response = new HashMap<>();
                        response.put("error", "User with id " + userId + " not found and no users available.");
                        return ResponseEntity.status(500).body(response);
                    }
                    userId = users.get(0).getId();
                }
            }
            
            Object productIdObj = request.get("productId");
            if (productIdObj == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "productId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long productId;
            try {
                if (productIdObj instanceof Number) {
                    productId = ((Number) productIdObj).longValue();
                } else {
                    productId = Long.parseLong(productIdObj.toString());
                }
            } catch (NumberFormatException e) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid productId format: " + productIdObj);
                return ResponseEntity.badRequest().body(response);
            }
            
            FavoriteDTO favorite = favoriteService.addFavorite(userId, productId);
            return ResponseEntity.ok(favorite);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        favoriteService.removeFavorite(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping
    public ResponseEntity<Void> clearAllFavorites(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                userId = users.get(0).getId(); // Используем первого доступного пользователя
            }
        }
        if (userId != null) {
            favoriteService.clearAllFavorites(userId);
        }
        return ResponseEntity.ok().build();
    }
}

