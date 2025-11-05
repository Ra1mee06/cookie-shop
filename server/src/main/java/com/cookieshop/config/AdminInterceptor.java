package com.cookieshop.config;

import com.cookieshop.entity.User;
import com.cookieshop.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class AdminInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public AdminInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "X-User-Id header is required for admin endpoints");
            return false;
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "User not found");
                return false;
            }
            User user = userOpt.get();
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Admin role required");
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid X-User-Id");
            return false;
        }
    }
}


