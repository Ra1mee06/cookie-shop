package com.cookieshop.controller;

import com.cookieshop.dto.SuggestionDTO;
import com.cookieshop.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suggestions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminSuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    @GetMapping
    public ResponseEntity<List<SuggestionDTO>> list(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ResponseEntity.ok(suggestionService.getSuggestionsByUserId(userId));
        }
        return ResponseEntity.ok(suggestionService.getAllSuggestions());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            suggestionService.deleteSuggestion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}


