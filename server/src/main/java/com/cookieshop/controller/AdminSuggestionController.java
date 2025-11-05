package com.cookieshop.controller;

import com.cookieshop.repository.SuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/suggestions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminSuggestionController {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @GetMapping
    public Object list(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return suggestionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return suggestionRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!suggestionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        suggestionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


