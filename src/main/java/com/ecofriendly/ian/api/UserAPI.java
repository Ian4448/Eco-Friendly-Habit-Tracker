package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.exceptions.VehicleNotFoundException;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.model.Vehicle;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class UserAPI {
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

    @GetMapping("/api/current-user")
    @ResponseBody
    public Map<String, String> getCurrentUser(HttpSession session, HttpServletRequest request) {
        String email = (String) session.getAttribute("userEmail");

        // If no session, check cookie
        if (email == null && request.getCookies() != null) {
            email = Arrays.stream(request.getCookies())
                    .filter(c -> "user_email".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return Collections.singletonMap("email", email);
    }

    @PutMapping("/modifyUserEmission")
    public void modifyUserEmissionCount(User user, String vehicleName, double distanceTravelled, boolean goodChoice) {
        try {
            Vehicle vehicle = user
                    .getVehicles()
                    .stream()
                    .filter(v -> v.getName().equals(vehicleName))
                    .findFirst()
                    .orElseThrow(VehicleNotFoundException::new);
            double carbonEmissionAmount = userService.calculateCarbonEmission(vehicle, distanceTravelled);
            userService.modifyUserCarbonEmissionTotal(user, carbonEmissionAmount, goodChoice);
        } catch (Exception e) {
            // handle exception
        }
    }
}
