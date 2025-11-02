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
    
    public Map<String, Object> register(String username, String email, String password, String fullName) {
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
        user.setRole("USER");
        
        User savedUser = userRepository.save(user);
        
        response.put("success", true);
        response.put("user", savedUser);
        response.put("message", "Registration successful");
        
        return response;
    }
}