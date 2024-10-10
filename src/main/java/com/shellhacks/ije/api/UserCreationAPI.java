package com.shellhacks.ije.api;

import com.shellhacks.ije.model.User;
import com.shellhacks.ije.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserCreationAPI {
    @Autowired
    private UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping({"/create", "/create/"})
    public String createUser(@RequestBody User user, Model model) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.addUser(user);
        model.addAttribute("user", user);
        return "SignUpPage";
    }

    // Handle the GET request for the form page
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "SignUpPage"; // Name of your HTML form page
    }
}
