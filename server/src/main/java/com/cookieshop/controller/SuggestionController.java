package com.cookieshop.controller;

import com.cookieshop.dto.SuggestionDTO;
import com.cookieshop.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class SuggestionController {
    
    @Autowired
    private SuggestionService suggestionService;
    
    @PostMapping
    public ResponseEntity<?> createSuggestion(
            @RequestBody SuggestionDTO suggestionDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        // Проверка авторизации
        if (userId == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Авторизация необходима для создания предложения. Пожалуйста, войдите в систему.");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            SuggestionDTO created = suggestionService.createSuggestion(suggestionDTO, userId);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ошибка при создании предложения: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<SuggestionDTO>> getSuggestionsByUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        if (userId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        List<SuggestionDTO> suggestions = suggestionService.getSuggestionsByUserId(userId);
        return ResponseEntity.ok(suggestions);
    }
}

