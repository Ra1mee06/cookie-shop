package com.cookieshop.service;

import com.cookieshop.entity.Product;
import com.cookieshop.entity.PromoCode;
import com.cookieshop.repository.ProductRepository;
import com.cookieshop.repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PromoCodeService {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<PromoCode> listAll() {
        return promoCodeRepository.findAll();
    }

    public Optional<PromoCode> findByCode(String code) {
        return promoCodeRepository.findByCodeIgnoreCase(code);
    }

    @Transactional
    public PromoCode create(PromoCode input) {
        if (input.getCode() == null || input.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }
        // Normalize
        input.setCode(input.getCode().trim().toUpperCase());
        // Resolve product by id if provided
        if (input.getProduct() != null && input.getProduct().getId() != null) {
            Product product = productRepository.findById(input.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            input.setProduct(product);
        }
        // Resolve product if provided by id in metadata is not handled here
        if (input.getExpiresAt() != null && input.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }
        return promoCodeRepository.save(input);
    }

    @Transactional
    public PromoCode update(Long id, PromoCode updates) {
        PromoCode existing = promoCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promo code not found"));
        if (updates.getCode() != null) existing.setCode(updates.getCode().trim().toUpperCase());
        if (updates.getType() != null) existing.setType(updates.getType());
        if (updates.getValue() != null) existing.setValue(updates.getValue());
        if (updates.getProduct() != null) {
            Long productId = updates.getProduct().getId();
            if (productId != null) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                existing.setProduct(product);
            }
        }
        if (updates.getMaxUses() != null) existing.setMaxUses(updates.getMaxUses());
        if (updates.getUsedCount() != null) existing.setUsedCount(updates.getUsedCount());
        if (updates.getExpiresAt() != null) existing.setExpiresAt(updates.getExpiresAt());
        existing.setActive(updates.isActive());
        if (updates.getMetadata() != null) existing.setMetadata(updates.getMetadata());
        return promoCodeRepository.save(existing);
    }

    @Transactional
    public void deactivate(Long id) {
        PromoCode existing = promoCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promo code not found"));
        existing.setActive(false);
        promoCodeRepository.save(existing);
    }
}


