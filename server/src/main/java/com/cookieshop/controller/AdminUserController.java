package com.cookieshop.controller;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import com.cookieshop.service.OrderService;
import com.cookieshop.service.SuggestionService;
import com.cookieshop.util.AdminActionLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin controller for user management:
 * - View all users
 * - View a specific user
 * - View user's orders and suggestions
 * - Edit user profile
 * - Delete user
 */
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

    @Autowired
    private AdminActionLogger adminLogger;

    /** List all users (admin only) */
    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        List<User> users = userRepository.findAll();
        adminLogger.log(admin, "Viewed all users (" + users.size() + ")");
        return ResponseEntity.ok(users);
    }

    /** Get full info about a user */
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        return userRepository.findById(id)
                .map(user -> {
                    adminLogger.log(admin, "Viewed user profile id=" + id);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Get all orders of a user */
    @GetMapping("/{id}/orders")
    public ResponseEntity<?> userOrders(@PathVariable Long id, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        List<OrderDTO> orders = orderService.getOrdersByUserId(id);
        adminLogger.log(admin, "Viewed orders of user id=" + id + " (" + orders.size() + ")");
        return ResponseEntity.ok(orders);
    }

    /** Get all suggestions (reviews/feedbacks) of a user */
    @GetMapping("/{id}/suggestions")
    public ResponseEntity<?> userSuggestions(@PathVariable Long id, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        Object suggestions = suggestionService.getSuggestionsByUserId(id);
        adminLogger.log(admin, "Viewed suggestions of user id=" + id);
        return ResponseEntity.ok(suggestions);
    }

    /** Update user profile fields */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> updates, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        return userRepository.findById(id).map(user -> {
            if (updates.containsKey("email")) user.setEmail(toStringOrNull(updates.get("email")));
            if (updates.containsKey("fullName")) user.setFullName(toStringOrNull(updates.get("fullName")));
            if (updates.containsKey("username")) user.setUsername(toStringOrNull(updates.get("username")));
            if (updates.containsKey("avatarUrl")) user.setAvatarUrl(toStringOrNull(updates.get("avatarUrl")));
            if (updates.containsKey("role")) {
                Object v = updates.get("role");
                if (v != null) user.setRole(v.toString().toUpperCase());
            }

            userRepository.save(user);
            adminLogger.log(admin, "Updated user id=" + id + " with fields " + updates.keySet());

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("user", user);
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Delete a user */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            adminLogger.log(admin, "Deleted user id=" + id);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String toStringOrNull(Object v) {
        return v == null ? null : v.toString();
    }
}
