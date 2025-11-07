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
    
    public Map<String, Object> login(String loginOrEmail, String password) {
        Map<String, Object> response = new HashMap<>();
        
        // Пытаемся найти пользователя по email или username
        Optional<User> userOptional = userRepository.findByEmail(loginOrEmail);
        if (!userOptional.isPresent()) {
            // Если не нашли по email, пробуем по username
            userOptional = userRepository.findByUsername(loginOrEmail);
        }
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Простое сравнение паролей без шифрования
            if (password.equals(user.getPassword())) {
                response.put("success", true);
                response.put("user", user);
                response.put("message", "Login successful");
            } else {
                response.put("success", false);
                response.put("message", "Неверный пароль");
            }
        } else {
            response.put("success", false);
            response.put("message", "Пользователь с таким логином или email не найден");
        }
        
        return response;
    }
    
    public Map<String, Object> register(String username, String email, String password, String fullName, String adminInviteCode) {
        Map<String, Object> response = new HashMap<>();
        
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.findByEmail(email).isPresent()) {
            response.put("success", false);
            response.put("message", "Email уже используется. Пожалуйста, используйте другой email.");
            return response;
        }
        
        // Логин теперь обязателен при регистрации
        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Логин обязателен для регистрации");
            return response;
        }
        
        String trimmedUsername = username.trim();
        
        // Валидация логина: от 3 до 20 символов, только буквы, цифры и подчеркивание
        if (!trimmedUsername.matches("^[a-zA-Z0-9_]{3,20}$")) {
            response.put("success", false);
            response.put("message", "Логин должен содержать от 3 до 20 символов (только буквы, цифры и подчеркивание)");
            return response;
        }
        
        // Проверяем уникальность логина
        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            response.put("success", false);
            response.put("message", "Логин уже используется. Пожалуйста, выберите другой логин.");
            return response;
        }
        
        username = trimmedUsername;
        
        // Создаем нового пользователя
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // Сохраняем пароль как есть
        user.setFullName(fullName);
        // Default role
        user.setRole("USER");
        
        // Save user first to get the ID
        User savedUser = userRepository.save(user);
        
        // If invite provided, consume and grant admin
        if (adminInviteCode != null && !adminInviteCode.trim().isEmpty()) {
            try {
                adminInviteService.consumeInviteOrThrow(adminInviteCode.trim(), savedUser.getId());
                savedUser.setRole("ADMIN");
                savedUser = userRepository.save(savedUser);
            } catch (IllegalArgumentException ex) {
                // Если код администратора неверный, удаляем созданного пользователя и возвращаем ошибку
                userRepository.delete(savedUser);
                response.put("success", false);
                String errorMessage = ex.getMessage();
                if (errorMessage.contains("not found")) {
                    response.put("message", "Код администратора не найден или неверный. Пожалуйста, проверьте введенный код администратора.");
                } else if (errorMessage.contains("already used")) {
                    response.put("message", "Код администратора уже использован. Пожалуйста, проверьте введенный код администратора.");
                } else if (errorMessage.contains("expired")) {
                    response.put("message", "Код администратора истек. Пожалуйста, проверьте введенный код администратора.");
                } else {
                    response.put("message", "Код администратора неверный. Пожалуйста, проверьте введенный код администратора.");
                }
                return response;
            }
        }
        
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