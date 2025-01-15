package com.ecofriendly.ian.controller;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.UserForm;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
public class UserLoginController {
    private static final Logger logger = Logger.getLogger(UserLoginController.class.getName());
    private final UserService userService;

    public UserLoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the login form for users. If an authentication token is found in the cookies
     * and is valid, the user is redirected to the home page without needing to log in again.
     * Otherwise, the login form is shown to the user.
     *
     * @param request the HTTP request containing the cookies
     * @param model the model used to add attributes for the view
     * @return a redirect to the home page if a valid authentication token is found, or the login form view otherwise
     */
    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            // Functionality to check for user's authentication if previously logged in with valid token.
            for (Cookie cookie : cookies) {
                logger.info("Found cookie: " + cookie.getName());
                if ("auth_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    logger.info("Found auth_token cookie with value: " + token);

                    if (userService.isTokenValid(token)) {
                        logger.info("Token is valid, proceeding with request.");
                        return "redirect:/home";
                    }
                }
            }
        }

        model.addAttribute("userForm", new UserForm("", "", (long) -1));
        return "loginform";
    }

    /**
     * Handles the login process for users. Validates the user's credentials and, if successful,
     * generates an authentication token, sets cookies, and stores the user's email in the session.
     * Redirects the user to the home page on successful login, or back to the login form with an
     * error message if authentication fails.
     *
     * @param userForm the form containing the user's login credentials (username and password)
     * @param model the model used to add attributes for the view
     * @param response the HTTP response used to set cookies
     * @param session the HTTP session used to store the user's email
     * @return a redirect to the home page if login is successful, or the login form view if login fails
     */
    @PostMapping("/login")
    public String loginPost(UserForm userForm, Model model, HttpServletResponse response, HttpSession session) {
        try {
            userForm.setPassword(Jsoup.clean(userForm.getPassword(), Safelist.none()));
            userForm.setUsername(Jsoup.clean(userForm.getUsername(), Safelist.none()));

            // Get userId directly, no need for separate matchLogin call
            Long userId = userService.matchLoginAndGetUserId(userForm);

            // if user is found and credentials match
            String token = UUID.randomUUID().toString();
            userService.storeToken(userId, token);  // Updated to use userId instead of username

            // Set auth token cookie
            Cookie authCookie = new Cookie("auth_token", token);
            authCookie.setHttpOnly(true);
            authCookie.setPath("/");
            authCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            response.addCookie(authCookie);

            // Set user id cookie
            Cookie userIdCookie = new Cookie("user_id", String.valueOf(userId));
            userIdCookie.setHttpOnly(false);
            userIdCookie.setPath("/");
            userIdCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            response.addCookie(userIdCookie);

            session.setAttribute("userId", userId);

            logger.info("Login successful for user ID: " + userId);
            return "redirect:/home";
        } catch (UserNotFoundException e) {
            model.addAttribute("error", "Invalid username or password");
            return "loginform";
        }
    }
}
