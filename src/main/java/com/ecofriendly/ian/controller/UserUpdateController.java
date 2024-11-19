package com.ecofriendly.ian.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class UserUpdateController {
    @GetMapping("/edit/{encoded_email}")
    public String userUpdate(
            Model model,
            @PathVariable String encoded_email,
            @CookieValue(value = "user_email", required = false) String authenticatedEmail) {

        if (authenticatedEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to access this page.");
        }

        try {
            String decodedEmail = URLDecoder.decode(encoded_email, StandardCharsets.UTF_8);

            if (!decodedEmail.equals(authenticatedEmail)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own settings.");
            }

            model.addAttribute("email", authenticatedEmail);
            return "UserUpdateForm";

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format in URL.");
        }
    }

    public static String encodeEmail(String email) {
        return URLEncoder.encode(email, StandardCharsets.UTF_8);
    }
}
