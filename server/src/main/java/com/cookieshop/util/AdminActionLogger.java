package com.cookieshop.util;

import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Utility class to verify admin user and log admin actions.
 */
@Component
public class AdminActionLogger {

    private final UserRepository userRepository;

    @Autowired
    public AdminActionLogger(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates that the user making the request is an ADMIN.
     * Returns the User entity for further use.
     */
    public User verifyAdmin(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header == null || header.isEmpty()) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        Long userId;
        try {
            userId = Long.parseLong(header);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid X-User-Id format");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User with id " + userId + " not found");
        }

        User user = userOpt.get();
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("User " + user.getEmail() + " is not an admin");
        }

        return user;
    }

    /**
     * Logs an admin action with timestamp and description.
     */
    public void log(User admin, String actionDescription) {
        System.out.printf("[%s] ADMIN %s (id=%d) performed action: %s%n",
                LocalDateTime.now(),
                admin.getEmail(),
                admin.getId(),
                actionDescription);
    }
}
