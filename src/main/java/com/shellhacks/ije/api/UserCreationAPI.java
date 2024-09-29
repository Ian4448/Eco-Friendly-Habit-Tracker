package com.shellhacks.ije.api;

import com.shellhacks.ije.model.User;
import com.shellhacks.ije.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserCreationAPI {
    @Autowired
    private UserService userService;

    @PostMapping({"/create", "/create/"})
    public String createUser(@ModelAttribute User user, Model model) {
        model.addAttribute("user", user);
        return "creationPage";
    }
}
