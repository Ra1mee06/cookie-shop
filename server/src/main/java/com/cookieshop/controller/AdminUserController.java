package com.cookieshop.controller;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import com.cookieshop.service.OrderService;
import com.cookieshop.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private SuggestionService suggestionService;

    @GetMapping
    public List<User> list() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/orders")
    public List<OrderDTO> userOrders(@PathVariable Long id) {
        return orderService.getOrdersByUserId(id);
    }

    @GetMapping("/{id}/suggestions")
    public Object userSuggestions(@PathVariable Long id) {
        return suggestionService.getSuggestionsByUserId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return userRepository.findById(id).map(user -> {
            if (updates.containsKey("email")) {
                Object v = updates.get("email");
                user.setEmail(v == null ? null : v.toString());
            }
            if (updates.containsKey("fullName")) {
                Object v = updates.get("fullName");
                user.setFullName(v == null ? null : v.toString());
            }
            if (updates.containsKey("username")) {
                Object v = updates.get("username");
                user.setUsername(v == null ? null : v.toString());
            }
            if (updates.containsKey("role")) {
                Object v = updates.get("role");
                if (v != null) user.setRole(v.toString().toUpperCase());
            }
            if (updates.containsKey("avatarUrl")) {
                Object v = updates.get("avatarUrl");
                user.setAvatarUrl(v == null ? null : v.toString());
            }
            userRepository.save(user);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("user", user);
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}


