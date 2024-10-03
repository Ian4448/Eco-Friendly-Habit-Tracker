package com.shellhacks.ije.controller;

import com.shellhacks.ije.model.User;
import com.shellhacks.ije.model.Vehicle;
import com.shellhacks.ije.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/addVehicle")
    public Vehicle addVehicle(Vehicle vehicle, User user) {
        try {
            vehicleService.addVehicle(vehicle, user.getEmail());
        } catch (Exception e) {
            // do nothing
        }
        return vehicle;
    }

    @PutMapping("/updateVehicle")
    public Vehicle updateVehicle(Vehicle vehicle, User user) {
        try {
            vehicleService.updateVehicle(vehicle, user.getEmail());
        } catch (Exception e) {
            // do nothing
        }
        return vehicle;
    }

    @GetMapping("/getVehicle")
    public Vehicle getVehicleById(UUID id, User user) {
        try {
            return vehicleService.getVehicle(user.getEmail(), id);
        } catch (Exception e) {
            // do nothing
        }
        return null;
    }

    @DeleteMapping
    public void deleteVehicle(UUID id, User user) {
        try {
            vehicleService.deleteVehicle(user.getEmail(), id);
        } catch (Exception e) {
            // do nothing
        }
    }
}
