package com.shellhacks.ije.api;

import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.model.UserForm;
import com.shellhacks.ije.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;
import java.util.logging.Logger;

@Controller
public class UserLoginAPI {
    private static final Logger logger = Logger.getLogger(UserLoginAPI.class.getName());
    @Autowired
    private UserService userService;

    // GET method to show login form
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
                        return "redirect:/home"; // prev removed for testing
                    } else {
                        logger.warning("Invalid token, redirecting to login.");
                    }
                }
            }
            }


//        logger.info("token:" + token);
//        if (token != null && userService.isTokenValid(token)) {
//            logger.info("Valid Token Found:" + token);
//            return "redirect:/home";
//        }

        // Add an empty UserForm to the model for form binding
        model.addAttribute("userForm", new UserForm("", ""));
        return "loginForm"; // Display login form view
    }

    // POST method to handle login logic
    @PostMapping("/login")
    public String loginPost(UserForm userForm, Model model, HttpServletResponse response) {
        try {
            boolean found = userService.matchLogin(userForm);
            if (found) {
                // Generate and store new token
                String token = UUID.randomUUID().toString();
                userService.storeToken(userForm.getUsername(), token);

                // Set token as cookie
                Cookie cookie = new Cookie("auth_token", token);
                cookie.setHttpOnly(true);
                cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                response.addCookie(cookie);
                logger.info("Added cookie: " + cookie);

                // Log success and redirect to a test page
                logger.info("Login successful for user: " + userForm.getUsername());
                model.addAttribute("message", "Login successful!");

                return "redirect:/home";
            } else {
                throw new UserNotFoundException("User not found");
            }
        } catch (UserNotFoundException e) {
            // Add error message to the model
            model.addAttribute("error", "Invalid username or password");
            logger.info("Invalid login" + e.getMessage());
            return "loginForm"; // Show login form with error message
        }
    }
}
