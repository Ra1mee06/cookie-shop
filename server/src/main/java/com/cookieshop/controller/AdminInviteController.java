package com.cookieshop.controller;

import com.cookieshop.entity.AdminInvite;
import com.cookieshop.service.AdminInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/invites")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminInviteController {

    @Autowired
    private AdminInviteService adminInviteService;

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-User-Id") Long userId,
                                    @RequestBody(required = false) Map<String, Object> body) {
        try {
            Integer hours = null;
            if (body != null && body.get("expiresInHours") instanceof Number) {
                hours = ((Number) body.get("expiresInHours")).intValue();
            }
            AdminInvite invite = adminInviteService.createInvite(userId, hours);
            return ResponseEntity.ok(invite);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}


