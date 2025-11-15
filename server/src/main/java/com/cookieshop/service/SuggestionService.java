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
        
        // Установка пользователя
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
    
    @Transactional(readOnly = true)
    public List<SuggestionDTO> getSuggestionsByUserId(Long userId) {
        List<Suggestion> suggestions = suggestionRepository.findByUserIdWithUser(userId);
        return suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SuggestionDTO> getAllSuggestions() {
        List<Suggestion> suggestions = suggestionRepository.findAllWithUser();
        return suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public void deleteSuggestion(Long id) {
        if (!suggestionRepository.existsById(id)) {
            throw new RuntimeException("Suggestion with id " + id + " not found");
        }
        suggestionRepository.deleteById(id);
    }
    
    private SuggestionDTO convertToDTO(Suggestion suggestion) {
        SuggestionDTO dto = new SuggestionDTO();
        dto.setId(suggestion.getId());
        
        if (suggestion.getUser() != null) {
            User user = suggestion.getUser();
            dto.setUserId(user.getId());
            dto.setUserEmail(user.getEmail());
            dto.setUserUsername(user.getUsername());
            dto.setUserFullName(user.getFullName());
        }
        
        dto.setAuthor(suggestion.getAuthor());
        dto.setProductName(suggestion.getProductName());
        dto.setDescription(suggestion.getDescription());
        dto.setCreatedAt(suggestion.getCreatedAt());
        dto.setUpdatedAt(suggestion.getUpdatedAt());
        
        return dto;
    }
}

