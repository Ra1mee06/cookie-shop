package com.cookieshop.controller;

import com.cookieshop.entity.Product;
import com.cookieshop.entity.PromoCode;
import com.cookieshop.repository.ProductRepository;
import com.cookieshop.repository.PromoCodeRepository;
import com.cookieshop.util.AdminActionLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin controller for managing promo codes.
 * Supports: list, detail, create, update, delete.
 */
@RestController
@RequestMapping("/api/admin/promos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminPromoController {

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AdminActionLogger adminLogger;

    /** List all promo codes */
    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        var admin = adminLogger.verifyAdmin(request);
        List<PromoCode> list = promoCodeRepository.findAll();
        adminLogger.log(admin, "Viewed all promo codes (" + list.size() + ")");
        return ResponseEntity.ok(list);
    }

    /** Get details of one promo */
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, HttpServletRequest request) {
        var admin = adminLogger.verifyAdmin(request);
        Optional<PromoCode> opt = promoCodeRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        adminLogger.log(admin, "Viewed promo code id=" + id);
        return ResponseEntity.ok(opt.get());
    }

    /** Create a new promo code */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        var admin = adminLogger.verifyAdmin(request);
        try {
            PromoCode promo = new PromoCode();

            promo.setCode((String) body.get("code"));
            promo.setDescription((String) body.getOrDefault("description", ""));
            promo.setType(parseType((String) body.get("type")));
            promo.setValue(parseBigDecimal(body.get("value")));
            promo.setActive(body.get("active") == null || Boolean.parseBoolean(body.get("active").toString()));
            promo.setMaxUses(parseInt(body.get("maxUses")));
            promo.setUsedCount(0);
            promo.setExpiresAt(parseDate(body.get("expiresAt")));
            promo.setMetadata((String) body.getOrDefault("metadata", null));

            // optional product link
            Long productId = parseLong(body.get("productId"));
            if (productId != null) {
                productRepository.findById(productId).ifPresent(promo::setProduct);
            }

            promoCodeRepository.save(promo);
            adminLogger.log(admin, "Created new promo code: " + promo.getCode());
            return ResponseEntity.ok(promo);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Failed to create promo: " + ex.getMessage());
        }
    }

    /** Update promo code */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        var admin = adminLogger.verifyAdmin(request);
        return promoCodeRepository.findById(id).map(promo -> {
            if (body.containsKey("code")) promo.setCode((String) body.get("code"));
            if (body.containsKey("description")) promo.setDescription((String) body.get("description"));
            if (body.containsKey("type")) promo.setType(parseType((String) body.get("type")));
            if (body.containsKey("value")) promo.setValue(parseBigDecimal(body.get("value")));
            if (body.containsKey("maxUses")) promo.setMaxUses(parseInt(body.get("maxUses")));
            if (body.containsKey("active")) promo.setActive(Boolean.parseBoolean(body.get("active").toString()));
            if (body.containsKey("expiresAt")) promo.setExpiresAt(parseDate(body.get("expiresAt")));
            if (body.containsKey("metadata")) promo.setMetadata((String) body.get("metadata"));

            Long productId = parseLong(body.get("productId"));
            if (productId != null) {
                productRepository.findById(productId).ifPresent(promo::setProduct);
            }

            promoCodeRepository.save(promo);
            adminLogger.log(admin, "Updated promo code id=" + id);
            return ResponseEntity.ok(promo);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Delete promo code */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        var admin = adminLogger.verifyAdmin(request);
        return promoCodeRepository.findById(id).map(promo -> {
            promoCodeRepository.delete(promo);
            adminLogger.log(admin, "Deleted promo code id=" + id);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // === Helpers ===

    private PromoCode.PromoType parseType(String s) {
        if (s == null) return PromoCode.PromoType.ORDER_PERCENT;
        try {
            return PromoCode.PromoType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PromoCode.PromoType.ORDER_PERCENT;
        }
    }

    private BigDecimal parseBigDecimal(Object v) {
        if (v == null) return null;
        try {
            return new BigDecimal(v.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(Object v) {
        if (v == null) return null;
        try {
            return Integer.parseInt(v.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(Object v) {
        if (v == null) return null;
        try {
            return Long.parseLong(v.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private OffsetDateTime parseDate(Object v) {
        if (v == null) return null;
        try {
            return OffsetDateTime.parse(v.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
