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
    @GetMapping("/edit/{id}")
    public String userUpdate(
            Model model,
            @PathVariable Long id,
            @CookieValue(value = "user_id", required = false) Long authenticatedId) {

        if (authenticatedId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to access this page.");
        }

        if (!id.equals(authenticatedId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own settings.");
        }

        model.addAttribute("userId", authenticatedId);
        return "userupdateform";
    }
}
