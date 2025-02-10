package com.ecofriendly.ian.interceptor;

import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.model.enums.UserRole;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(AuthenticationInterceptor.class.getName());

    public AuthenticationInterceptor(UserService userService) {
        this.userService = userService;
    }

    private static final List<String> ADMIN_ENDPOINTS = Arrays.asList(
            "/api/users",
            "/api/deleteUser"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    String token = cookie.getValue();

                    // Get user from token
                    User user = userService.getUserByToken(token);

                    if (user != null) {
                        // Check if endpoint requires admin
                        if (ADMIN_ENDPOINTS.stream().anyMatch(path::startsWith)) {
                            if (user.getRole() != UserRole.ADMIN) {
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                                return false;
                            }
                        }
                        // User is authenticated and authorized
                        return true;
                    }
                }
            }
        }

        response.sendRedirect("/login");
        return false;
    }
}
