package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.exceptions.VehicleNotFoundException;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.service.EmissionService;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class UserAPI {
    @Autowired
    private UserService userService;
    @Autowired
    private EmissionService emissionService;

    private final Logger logger = Logger.getLogger(UserAPI.class.getName());

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

    @PutMapping("/api/modifyUserEmission")
    public ResponseEntity<Object> logUserEmissionAndDistanceCount(@RequestBody EmissionRequest request)
            throws UserNotFoundException, VehicleNotFoundException {
//        System.out.println("good:"+request.isGoodChoice());
//        System.out.printf("vehicle name: %s, distnace traveled: %f, user email: %s, transportation %s",
//                request.getVehicleName(), request.getDistanceTravelled(), request.getUserEmail(),request.getTransportation());
        try {
            User user = userService.getUserByEmail(URLDecoder.decode(request.getUserEmail(), StandardCharsets.UTF_8));
//            logger.info("email after decoding " + user.getEmail());

            if (request.getTransportation() == TransportationType.CAR) {
                Vehicle vehicle = user
                        .getVehicles()
                        .stream()
                        .filter(v -> v.getName().equals(request.getVehicleName()))
                        .findFirst()
                        .orElseThrow(VehicleNotFoundException::new);

                double carbonEmissionAmount = emissionService.calculateCarbonEmission(vehicle, request.getDistanceTravelled());
                emissionService.modifyUserCarbonEmissionTotal(request.getTime(), user, carbonEmissionAmount, request.isGoodChoice());
            }

            emissionService.modifyUserDistance(request.getTime(), user, request.getTransportation(), request.getDistanceTravelled());
//            logger.info("Daily amount of emission: " + user.getEmission().getDailyEmissionCount());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return (ResponseEntity<Object>) ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
    }
}
