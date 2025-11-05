package com.cookieshop.controller;

import com.cookieshop.entity.PromoCode;
import com.cookieshop.service.PromoCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promocodes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminPromoController {

    @Autowired
    private PromoCodeService promoCodeService;

    @GetMapping
    public List<PromoCode> list() {
        return promoCodeService.listAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PromoCode input) {
        try {
            return ResponseEntity.ok(promoCodeService.create(input));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PromoCode updates) {
        try {
            return ResponseEntity.ok(promoCodeService.update(id, updates));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        try {
            promoCodeService.deactivate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}


