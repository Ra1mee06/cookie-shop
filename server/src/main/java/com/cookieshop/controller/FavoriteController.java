package com.cookieshop.controller;

import com.cookieshop.dto.FavoriteDTO;
import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import com.cookieshop.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        // Если userId не передан, возвращаем пустой список (для неавторизованных пользователей)
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }
        
        List<FavoriteDTO> favorites = favoriteService.getAllFavoritesByUserId(userId);
        return ResponseEntity.ok(favorites);
    }
    
    @PostMapping
    public ResponseEntity<?> addFavorite(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        try {
            // Если userId не передан, используем первого доступного пользователя для тестирования
            // В будущем нужно получать из токена
            if (userId == null) {
                List<User> users = userRepository.findAll();
                if (users.isEmpty()) {
                    return ResponseEntity.status(500)
                        .body(Map.of("error", "No users found in database. Please create a user first."));
                }
                userId = users.get(0).getId(); // Используем первого доступного пользователя
            } else {
                // Проверяем, существует ли указанный пользователь
                if (!userRepository.findById(userId).isPresent()) {
                    List<User> users = userRepository.findAll();
                    if (users.isEmpty()) {
                        return ResponseEntity.status(500)
                            .body(Map.of("error", "User with id " + userId + " not found and no users available."));
                    }
                    // Используем первого доступного пользователя
                    userId = users.get(0).getId();
                }
            }
            
            Object productIdObj = request.get("productId");
            if (productIdObj == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "productId is required"));
            }
            
            Long productId;
            try {
                if (productIdObj instanceof Number) {
                    productId = ((Number) productIdObj).longValue();
                } else {
                    productId = Long.parseLong(productIdObj.toString());
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid productId format: " + productIdObj));
            }
            
            FavoriteDTO favorite = favoriteService.addFavorite(userId, productId);
            return ResponseEntity.ok(favorite);
        } catch (RuntimeException e) {
            e.printStackTrace(); // Логируем ошибку
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Логируем ошибку
            return ResponseEntity.status(500)
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
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

