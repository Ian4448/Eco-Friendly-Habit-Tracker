package com.ecofriendly.ian.api;

import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.model.Vehicle;
import com.ecofriendly.ian.service.UserService;
import com.ecofriendly.ian.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class VehicleAPI {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @PostMapping("/addVehicle")
    public ResponseEntity<?> addVehicle(@CookieValue(value = "auth_token", required = false) String token, @RequestBody Vehicle vehicle) {
        try {
            User user = userService.getUserByToken(token);

            if (vehicleService.isVehicleNameUsed(vehicle.getName(), user.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "error", true,
                                "message", String.format("Vehicle name '%s' is already in use", vehicle.getName())
                        ));
            }

            if (vehicleService.userVehicleCountExceeded(user.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of("error", true, "message",
                                String.format("Vehicle count cannot be greater than %d", vehicleService.getMaximumVehiclePerUser())));
            }

            Vehicle savedVehicle = vehicleService.addVehicle(vehicle, user.getEmail());
            return ResponseEntity.ok(savedVehicle);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", true,
                            "message", "An error occurred while adding the vehicle"
                    ));
        }
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

    @DeleteMapping("/deleteVehicle")
    public void deleteVehicle(@CookieValue(value = "auth_token", required = false) String token, @RequestParam String vehicleName) {
        try {
            User user = userService.getUserByToken(token);
            vehicleService.deleteVehicle(
                    user.getEmail(), vehicleService.getVehicleFromName(vehicleName, user.getEmail()));
        } catch (Exception e) {
            // do nothing
        }
    }
}

