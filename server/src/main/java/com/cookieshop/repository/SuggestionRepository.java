package com.cookieshop.repository;

import com.cookieshop.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT s FROM Suggestion s LEFT JOIN FETCH s.user ORDER BY s.createdAt DESC")
    List<Suggestion> findAllWithUser();
    
    @Query("SELECT s FROM Suggestion s LEFT JOIN FETCH s.user WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Suggestion> findByUserIdWithUser(Long userId);
}

