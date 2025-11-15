package com.cookieshop.controller;

import com.cookieshop.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String loginOrEmail = credentials.get("loginOrEmail") != null ? credentials.get("loginOrEmail") : credentials.get("email");
        String password = credentials.get("password");
        
        if (loginOrEmail == null || loginOrEmail.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Введите логин или email");
            return ResponseEntity.badRequest().body(response);
        }
        
        Map<String, Object> result = authService.login(loginOrEmail.trim(), password);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            String username = userData.get("username");
            String email = userData.get("email");
            String password = userData.get("password");
            String fullName = userData.get("fullName");
            
            // Валидация обязательных полей
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Email обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            if (username == null || username.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Логин обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            if (password == null || password.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Пароль обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            if (fullName == null || fullName.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Имя обязательно");
                return ResponseEntity.badRequest().body(response);
            }
            
            String adminInviteCode = userData.get("adminInviteCode");
            Map<String, Object> result = authService.register(username, email, password, fullName, adminInviteCode);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> profileData,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Authorization required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            String email = profileData.get("email");
            String fullName = profileData.get("fullName");
            String username = profileData.get("username");
            
            Map<String, Object> result = authService.updateProfile(userId, email, fullName, username);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PutMapping("/profile/password")
    public ResponseEntity<?> updatePassword(
            @RequestBody Map<String, String> passwordData,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Authorization required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");
            
            // Валидация
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Old password is required");
                return ResponseEntity.badRequest().body(response);
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "New password is required");
                return ResponseEntity.badRequest().body(response);
            }
            if (!newPassword.equals(confirmPassword)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }
            if (newPassword.length() < 6) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Password must be at least 6 characters");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = authService.updatePassword(userId, oldPassword, newPassword);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/profile/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Authorization required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            String finalAvatarUrl = avatarUrl;
            
            // Сохранение аватара
            if (file != null && !file.isEmpty()) {
                String uploadDir = "uploads/avatars";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
                String filename = UUID.randomUUID().toString() + extension;
                
                Path filePath = Paths.get(uploadDir, filename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                finalAvatarUrl = "/uploads/avatars/" + filename;
            }
            if (finalAvatarUrl != null && !finalAvatarUrl.trim().isEmpty()) {
                Map<String, Object> result = authService.updateAvatar(userId, finalAvatarUrl);
                
                if ((Boolean) result.get("success")) {
                    return ResponseEntity.ok(result);
                } else {
                    return ResponseEntity.badRequest().body(result);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Avatar URL or file is required");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}