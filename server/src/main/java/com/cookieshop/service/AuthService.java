package com.cookieshop.service;

import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminInviteService adminInviteService;
    
    public Map<String, Object> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        Map<String, Object> response = new HashMap<>();
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Простое сравнение паролей без шифрования
            if (password.equals(user.getPassword())) {
                response.put("success", true);
                response.put("user", user);
                response.put("message", "Login successful");
            } else {
                response.put("success", false);
                response.put("message", "Invalid password");
            }
        } else {
            response.put("success", false);
            response.put("message", "User not found");
        }
        
        return response;
    }
    
    public Map<String, Object> register(String username, String email, String password, String fullName, String adminInviteCode) {
        Map<String, Object> response = new HashMap<>();
        
        // Генерируем username из email, если он не передан
        if (username == null || username.trim().isEmpty()) {
            // Берем часть email до @ символа
            String emailPrefix = email.split("@")[0];
            username = emailPrefix;
            
            // Проверяем уникальность и добавляем суффикс, если нужно
            int suffix = 1;
            String originalUsername = username;
            while (userRepository.findByUsername(username).isPresent()) {
                username = originalUsername + suffix;
                suffix++;
            }
        }
        
        // Проверяем, существует ли пользователь
        if (userRepository.findByEmail(email).isPresent()) {
            response.put("success", false);
            response.put("message", "Email already exists");
            return response;
        }
        
        // Проверяем уникальность username (для случая, если он был передан вручную)
        // Если username был автогенерирован, его уникальность уже гарантирована выше
        if (userRepository.findByUsername(username).isPresent()) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return response;
        }
        
        // Создаем нового пользователя
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // Сохраняем пароль как есть
        user.setFullName(fullName);
        // Default role
        user.setRole("USER");
        // If invite provided, consume and grant admin
        if (adminInviteCode != null && !adminInviteCode.trim().isEmpty()) {
            try {
                adminInviteService.consumeInviteOrThrow(adminInviteCode.trim());
                user.setRole("ADMIN");
            } catch (IllegalArgumentException ex) {
                // leave role as USER; attach message
                response.put("inviteError", ex.getMessage());
            }
        }
        
        User savedUser = userRepository.save(user);
        
        response.put("success", true);
        response.put("user", savedUser);
        response.put("message", "Registration successful");
        
        return response;
    }
    
    public Map<String, Object> updateProfile(Long userId, String email, String fullName, String username) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }
        
        User user = userOptional.get();
        
        // Проверяем уникальность email, если он изменился
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.findByEmail(email).isPresent()) {
                response.put("success", false);
                response.put("message", "Email already exists");
                return response;
            }
            user.setEmail(email);
        }
        
        // Проверяем уникальность username, если он изменился
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.findByUsername(username).isPresent()) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return response;
            }
            user.setUsername(username);
        }
        
        // Обновляем fullName
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        
        User updatedUser = userRepository.save(user);
        
        response.put("success", true);
        response.put("user", updatedUser);
        response.put("message", "Profile updated successfully");
        
        return response;
    }
    
    public Map<String, Object> updatePassword(Long userId, String oldPassword, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }
        
        User user = userOptional.get();
        
        // Проверяем старый пароль
        if (!oldPassword.equals(user.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid old password");
            return response;
        }
        
        // Устанавливаем новый пароль
        user.setPassword(newPassword);
        userRepository.save(user);
        
        response.put("success", true);
        response.put("message", "Password updated successfully");
        
        return response;
    }
    
    public Map<String, Object> updateAvatar(Long userId, String avatarUrl) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }
        
        User user = userOptional.get();
        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);
        
        response.put("success", true);
        response.put("user", updatedUser);
        response.put("message", "Avatar updated successfully");
        
        return response;
    }
}