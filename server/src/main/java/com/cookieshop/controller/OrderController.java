package com.cookieshop.controller;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.repository.UserRepository;
import com.cookieshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, Object> orderData,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        try {
            // Проверка авторизации - userId обязателен
            if (userId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Авторизация необходима для создания заказа. Пожалуйста, войдите в систему.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Проверяем, существует ли указанный пользователь
            if (!userRepository.findById(userId).isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Пользователь с id " + userId + " не найден. Пожалуйста, войдите в систему снова.");
                return ResponseEntity.status(401).body(response);
            }
            
            OrderDTO order = orderService.createOrder(orderData, userId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            String errorMessage = e.getMessage();
            response.put("error", errorMessage);
            // Если ошибка связана с промокодом, возвращаем статус 400 (Bad Request)
            // В противном случае возвращаем 500 (Internal Server Error)
            if (errorMessage != null && (errorMessage.contains("промокод") || errorMessage.contains("Промокод") || 
                errorMessage.contains("promo") || errorMessage.contains("Promo"))) {
                return ResponseEntity.status(400).body(response);
            }
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        // Если userId не передан, возвращаем пустой список
        if (userId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }
}

