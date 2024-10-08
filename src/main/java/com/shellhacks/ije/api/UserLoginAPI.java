package com.shellhacks.ije.api;

import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.model.UserForm;
import com.shellhacks.ije.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserLoginAPI {
    @Autowired
    private UserService userService;

    // GET method to show login form
    @GetMapping("/login")
    public String loginForm(Model model) {
        // Add an empty UserForm to the model for form binding
        model.addAttribute("userForm", new UserForm("", ""));
        return "loginForm"; // Display login form view
    }

    // POST method to handle login logic
    @PostMapping("/login")
    public String loginPost(UserForm userForm, Model model) {
        try {
            boolean found = userService.matchLogin(userForm);
            if (found) {
                // Log success and redirect to a test page
                System.out.println("Login successful for user: " + userForm.getUsername());
                model.addAttribute("message", "Login successful!");

                return "redirect:/home";
            } else {
                throw new UserNotFoundException("User not found");
            }
        } catch (UserNotFoundException e) {
            // Add error message to the model
            model.addAttribute("error", "Invalid username or password");
            System.out.println("Login failed: " + e.getMessage());
            return "loginForm"; // Show login form with error message
        }
    }
}
