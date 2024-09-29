package com.shellhacks.ije.controller;

import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.model.User;
import com.shellhacks.ije.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping({"/addCustomer", "/addCustomer/"})
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping({"/updateUser", "/updateUser/"})
    public User updateUser(@RequestBody User user) {
        try {
            userService.updateUser(user.getEmail(), user);
            return user;
        } catch (UserNotFoundException e) {
            // template user
            return new User();
        }
    }


    @GetMapping({"/users", "/users/"})
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping({"/user", "/user/"})
    public User getUser(@RequestParam(value = "email", required = false) String email) {
        try {
            return userService.getUserByEmail(email);
        } catch (UserNotFoundException e) {
            return new User();
        }
    }

    @DeleteMapping({"/deleteUser", "/deleteUser/"})
    public void deleteUser(@RequestParam String email) {
        try {
            userService.deleteUser(userService.getUserByEmail(email));
        } catch (UserNotFoundException e) {
            // do nothing
        }
    }
}
