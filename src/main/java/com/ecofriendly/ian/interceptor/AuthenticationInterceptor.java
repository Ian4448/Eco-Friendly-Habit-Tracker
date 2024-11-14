package com.ecofriendly.ian.interceptor;

import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.logging.Logger;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(AuthenticationInterceptor.class.getName());

    public AuthenticationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("Checking cookies for authentication...");
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                logger.info("Found cookie: " + cookie.getName());
                if ("auth_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    logger.info("Found auth_token cookie with value: " + token);

                    if (userService.isTokenValid(token)) {
                        logger.info("Token is valid, proceeding with request.");
                        return true;
                    } else {
                        logger.warning("Invalid token, redirecting to login.");
                    }
                }
            }
        } else {
            logger.info("No cookies found.");
        }

        response.sendRedirect("/login");
        return false;
    }
}
