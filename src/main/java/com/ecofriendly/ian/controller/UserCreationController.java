package com.ecofriendly.ian.controller;

import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.logging.Logger;

@Controller
public class UserCreationController {
    @Autowired
    private UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Logger logger = Logger.getLogger(UserCreationController.class.getName());

    @PostMapping({"/create", "/create/"})
    public String createUser(@RequestBody User user, Model model) {
        // Sanitize user inputs
        String sanitizedFirstName = Jsoup.clean(user.getFirstName(), Safelist.none());
        String sanitizedLastName = Jsoup.clean(user.getLastName(), Safelist.none());
        String sanitizedEmail = Jsoup.clean(user.getEmail(), Safelist.none());

//        logger.info(sanitizedFirstName
//                + sanitizedLastName + sanitizedEmail + user.getPassword());

        // Set the sanitized values back to the user object
        user.setFirstName(sanitizedFirstName);
        user.setLastName(sanitizedLastName);
        user.setEmail(sanitizedEmail);

        // Hash the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Add the user to the database
        userService.addUser(user);

        // Add the user to the model for the view
        model.addAttribute("user", user);

        return "SignUpPage"; // Redirect to the signup page after creation
    }

    // Handle the GET request for the form page
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "SignUpPage"; // Name of your HTML form page
    }
}
