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
import java.util.logging.Logger;

@RestController
public class VehicleAPI {
    private final VehicleService vehicleService;
    private final UserService userService;

    private final Logger logger = Logger.getLogger(VehicleAPI.class.getName());

    public VehicleAPI(VehicleService vehicleService, UserService userService) {
        this.vehicleService = vehicleService;
        this.userService = userService;
    }

    /**
     * Adds a new vehicle for the authenticated user. Validates the vehicle's uniqueness and ensures the user
     * has not exceeded the maximum allowed number of vehicles. If validation passes, the vehicle is added
     * to the user's account.
     *
     * @param token the authentication token extracted from the cookies
     * @param vehicle the vehicle object containing the details of the vehicle to be added
     * @return a {@link ResponseEntity} containing the added vehicle if successful, or an error message if
     *         the request fails validation or encounters an internal error
     */
    @PostMapping("/api/addVehicle")
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

    @GetMapping("/api/getVehicles")
    public List<Vehicle> getVehicles(@CookieValue(value = "auth_token", required = false) String token) {
        try {
            User user = userService.getUserByToken(token); // Get the user by token
            return vehicleService.getAllVehicles(user.getEmail());
        } catch (Exception e) {
            logger.info("Failed to retrieve list of vehicles.");
        }
        return null;
    }

    @DeleteMapping("/api/deleteVehicle")
    public void deleteVehicle(@CookieValue(value = "auth_token", required = false) String token, @RequestParam String vehicleName) {
        try {
            User user = userService.getUserByToken(token);
            vehicleService.deleteVehicle(
                    user.getEmail(), vehicleService.getVehicleFromName(vehicleName, user.getEmail()));
        } catch (Exception e) {
            logger.info("Failed to delete the requested vehicle.");
        }
    }
}

