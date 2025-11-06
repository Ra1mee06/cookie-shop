package com.cookieshop.service;

import com.cookieshop.entity.AdminInvite;
import com.cookieshop.entity.User;
import com.cookieshop.repository.AdminInviteRepository;
import com.cookieshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AdminInviteService {

    @Autowired
    private AdminInviteRepository adminInviteRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AdminInvite createInvite(Long createdByUserId, Integer expiresInHours) {
        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        if (!"ADMIN".equalsIgnoreCase(creator.getRole())) {
            throw new IllegalArgumentException("Only admin can create invites");
        }
        AdminInvite invite = new AdminInvite();
        invite.setCode(UUID.randomUUID().toString());
        invite.setCreatedBy(creator);
        if (expiresInHours != null && expiresInHours > 0) {
            invite.setExpiresAt(OffsetDateTime.now().plusHours(expiresInHours));
        }
        return adminInviteRepository.save(invite);
    }

    @Transactional
    public void consumeInviteOrThrow(String code, Long userId) {
        AdminInvite invite = adminInviteRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Invite code not found"));
        if (invite.isUsed()) {
            throw new IllegalArgumentException("Invite already used");
        }
        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Invite expired");
        }
        invite.setUsed(true);
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            invite.setUsedBy(user);
        }
        adminInviteRepository.save(invite);
    }
}


