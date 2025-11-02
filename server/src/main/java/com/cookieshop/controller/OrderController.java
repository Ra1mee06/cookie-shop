package com.cookieshop.controller;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import com.cookieshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            
            OrderDTO order = orderService.createOrder(orderData, userId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        // Если userId не передан, возвращаем пустой список
        if (userId == null) {
            return ResponseEntity.ok(List.of());
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
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        }
    }
}

