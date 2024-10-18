package com.shellhacks.ije.controller;

import com.shellhacks.ije.model.User;
import com.shellhacks.ije.model.Vehicle;
import com.shellhacks.ije.service.UserService;
import com.shellhacks.ije.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @PostMapping("/addVehicle")
    public Vehicle addVehicle(@CookieValue(value = "auth_token", required = false) String token, @RequestBody Vehicle vehicle) {
        try {
            User user = userService.getUserByToken(token); // Get the user by token
            return vehicleService.addVehicle(vehicle, user.getEmail());
        } catch (Exception e) {
            // Handle error
        }
        return null;
    }

    @GetMapping("/getVehicles")
    public List<Vehicle> getVehicles(@CookieValue(value = "auth_token", required = false) String token) {
        try {
            User user = userService.getUserByToken(token); // Get the user by token
            return vehicleService.getAllVehicles(user.getEmail());
        } catch (Exception e) {
            // Handle error
        }
        return null;
    }
}

