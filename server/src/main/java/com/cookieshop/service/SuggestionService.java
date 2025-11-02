package com.cookieshop.service;

import com.cookieshop.dto.SuggestionDTO;
import com.cookieshop.entity.Suggestion;
import com.cookieshop.entity.User;
import com.cookieshop.repository.SuggestionRepository;
import com.cookieshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SuggestionService {
    
    @Autowired
    private SuggestionRepository suggestionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public SuggestionDTO createSuggestion(SuggestionDTO suggestionDTO, Long userId) {
        Suggestion suggestion = new Suggestion();
        
        // Устанавливаем пользователя
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                suggestion.setUser(userOpt.get());
            }
        }
        
        suggestion.setAuthor(suggestionDTO.getAuthor());
        suggestion.setProductName(suggestionDTO.getProductName());
        suggestion.setDescription(suggestionDTO.getDescription());
        
        Suggestion saved = suggestionRepository.save(suggestion);
        
        return convertToDTO(saved);
    }
    
    public List<SuggestionDTO> getSuggestionsByUserId(Long userId) {
        List<Suggestion> suggestions = suggestionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private SuggestionDTO convertToDTO(Suggestion suggestion) {
        SuggestionDTO dto = new SuggestionDTO();
        dto.setId(suggestion.getId());
        
        if (suggestion.getUser() != null) {
            dto.setUserId(suggestion.getUser().getId());
        }
        
        dto.setAuthor(suggestion.getAuthor());
        dto.setProductName(suggestion.getProductName());
        dto.setDescription(suggestion.getDescription());
        dto.setCreatedAt(suggestion.getCreatedAt());
        dto.setUpdatedAt(suggestion.getUpdatedAt());
        
        return dto;
    }
}

