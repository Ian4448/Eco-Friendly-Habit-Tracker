package com.ecofriendly.ian.controller;

import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.logging.Logger;

@Controller
public class UserCreationController {
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Logger logger = Logger.getLogger(UserCreationController.class.getName());

    public UserCreationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles user creation by sanitizing input, hashing the password, and storing the user in the database.
     * After successful user creation, the user is added to the model and redirected to the signup page.
     *
     * @param user the user object containing the user's details (first name, last name, email, and password)
     * @param model the model used to add the user attributes for the view
     * @return the name of the signup page view
     */
    @PostMapping({"/create", "/create/"})
    public String createUser(@RequestBody User user, Model model) {
        String sanitizedFirstName = Jsoup.clean(user.getFirstName(), Safelist.none());
        String sanitizedLastName = Jsoup.clean(user.getLastName(), Safelist.none());
        String sanitizedEmail = Jsoup.clean(user.getEmail(), Safelist.none());

        user.setFirstName(sanitizedFirstName);
        user.setLastName(sanitizedLastName);
        user.setEmail(sanitizedEmail);

        // Hash the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Add the user to the database
        userService.addUser(user);

        // Add the user to the model for the view
        model.addAttribute("user", user);

        return "signuppage"; // Redirect to the signup page after creation
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "signuppage";
    }
}
