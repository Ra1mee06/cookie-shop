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
        
        // Поиск пользователя
        Optional<User> userOptional = userRepository.findByEmail(loginOrEmail);
        if (!userOptional.isPresent()) {
            userOptional = userRepository.findByUsername(loginOrEmail);
        }
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
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
        
        // Проверка существования пользователя
        if (userRepository.findByEmail(email).isPresent()) {
            response.put("success", false);
            response.put("message", "Email уже используется. Пожалуйста, используйте другой email.");
            return response;
        }
        
        if (username == null || username.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Логин обязателен для регистрации");
            return response;
        }
        
        String trimmedUsername = username.trim();
        
        // Валидация логина
        if (!trimmedUsername.matches("^[a-zA-Z0-9_]{3,20}$")) {
            response.put("success", false);
            response.put("message", "Логин должен содержать от 3 до 20 символов (только буквы, цифры и подчеркивание)");
            return response;
        }
        
        // Проверка уникальности логина
        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            response.put("success", false);
            response.put("message", "Логин уже используется. Пожалуйста, выберите другой логин.");
            return response;
        }
        
        username = trimmedUsername;
        
        // Добавление в БД
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole("USER");
        
        // Сохранение пользователя
        User savedUser = userRepository.save(user);
        
        // Обработка приглашения администратора
        if (adminInviteCode != null && !adminInviteCode.trim().isEmpty()) {
            try {
                adminInviteService.consumeInviteOrThrow(adminInviteCode.trim(), savedUser.getId());
                savedUser.setRole("ADMIN");
                savedUser = userRepository.save(savedUser);
            } catch (IllegalArgumentException ex) {
                // Удаление пользователя при ошибке
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
        
        // Проверка уникальности email
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.findByEmail(email).isPresent()) {
                response.put("success", false);
                response.put("message", "Email already exists");
                return response;
            }
            user.setEmail(email);
        }
        
        // Проверка уникальности username
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.findByUsername(username).isPresent()) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return response;
            }
            user.setUsername(username);
        }
        
        // Обновление профиля
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
        
        // Проверка старого пароля
        if (!oldPassword.equals(user.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid old password");
            return response;
        }
        
        // Изменение пароля
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