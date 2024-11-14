package com.shellhacks.ije.api;

import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.model.UserForm;
import com.shellhacks.ije.service.UserService;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
public class UserLoginAPI {
    private static final Logger logger = Logger.getLogger(UserLoginAPI.class.getName());
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                logger.info("Found cookie: " + cookie.getName());
                if ("auth_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    logger.info("Found auth_token cookie with value: " + token);

                    if (userService.isTokenValid(token)) {
                        logger.info("Token is valid, proceeding with request.");
                        return "redirect:/home";
                    } else {
                        logger.warning("Invalid token, redirecting to login.");
                    }
                }
            }
        }

        model.addAttribute("userForm", new UserForm("", ""));
        return "loginForm";
    }

    @PostMapping("/login")
    public String loginPost(UserForm userForm, Model model, HttpServletResponse response, HttpSession session) {
        try {
            boolean found = userService.matchLogin(userForm);
            if (found) {
                // Generate and store new token
                String token = UUID.randomUUID().toString();
                userService.storeToken(userForm.getUsername(), token);

                // Set auth token cookie
                Cookie authCookie = new Cookie("auth_token", token);
                authCookie.setHttpOnly(true);
                authCookie.setPath("/");
                authCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                response.addCookie(authCookie);

                // Set user email cookie
                Cookie emailCookie = new Cookie("user_email", userForm.getUsername());
                emailCookie.setHttpOnly(false);
                emailCookie.setPath("/");
                emailCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                response.addCookie(emailCookie);

                // Store email in session
                session.setAttribute("userEmail", userForm.getUsername());

                logger.info("Login successful for user: " + userForm.getUsername());
                return "redirect:/home";
            } else {
                throw new UserNotFoundException("User not found");
            }
        } catch (UserNotFoundException e) {
            model.addAttribute("error", "Invalid username or password");
            logger.info("Invalid login" + e.getMessage());
            return "loginForm";
        }
    }

    // Add this new endpoint to get the current user's email
    @GetMapping("/api/current-user")
    @ResponseBody
    public Map<String, String> getCurrentUser(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        return Collections.singletonMap("email", email);
    }
}
