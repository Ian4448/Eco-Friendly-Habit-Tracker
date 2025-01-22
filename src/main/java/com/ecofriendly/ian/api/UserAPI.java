package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.exceptions.VehicleNotFoundException;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.service.EmissionService;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

@RestController
public class UserAPI {
    private final Logger logger = Logger.getLogger(UserAPI.class.getName());
    private final UserService userService;
    private final EmissionService emissionService;

    public UserAPI(UserService userService, EmissionService emissionService) {
        this.userService = userService;
        this.emissionService = emissionService;
    }


    @PostMapping({"/api/addCustomer", "/api/addCustomer/"})
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping("/api/updateUser")
    public User updateUser(@RequestBody User userDetails, @CookieValue("user_id") String userId) throws UserNotFoundException {
        Long userIdLong = Long.parseLong(userId);

        User currentUser = userService.getUserById(userIdLong);

        return userService.updateUser(currentUser.getEmail(), userDetails);
    }


    @GetMapping({"/api/users", "/api/users/"})
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping({"/user", "/user/"}) //deprecated
    public User getUser(@RequestParam(value = "email", required = false) String email) {
        try {
            return userService.getUserByEmail(email);
        } catch (UserNotFoundException e) {
            return new User();
        }
    }

    @DeleteMapping({"/api/deleteUser", "/api/deleteUser/"})
    public void deleteUser(@RequestParam String email) {
        try {
            userService.deleteUser(userService.getUserByEmail(email));
        } catch (UserNotFoundException e) {
            // do nothing
        }
    }

    @GetMapping("/api/current-user") //deprecated
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

    @GetMapping("/api/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);

            Map<String, Object> userDto = new HashMap<>();
            userDto.put("id", user.getId());
            userDto.put("email", user.getEmail());
            userDto.put("firstName", user.getFirstName());
            userDto.put("lastName", user.getLastName());
            return ResponseEntity.ok(userDto);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        Cookie[] cookies = request.getCookies();
        session.invalidate();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");  // This is important!
                    response.addCookie(cookie);  // Need to add to response
                    logger.info("Deleted auth_token cookie");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/"))
                .build();
    }

    @PutMapping("/api/modifyUserEmission")
    public ResponseEntity<Object> logUserEmissionAndDistanceCount(@RequestBody EmissionRequest request) {
        boolean goodChoice = request.getTransportation() != TransportationType.CAR;

        try {
            User user = userService.getUserById(request.getUserId());

            Vehicle vehicle = user
                    .getVehicles()
                    .stream()
                    .filter(v -> v.getName().equals(request.getVehicleName()))
                    .findFirst()
                    .orElseThrow(VehicleNotFoundException::new);

            double carbonEmissionAmount = emissionService.calculateCarbonEmission(vehicle, request.getDistanceTravelled());
            emissionService.addUserCarbonEmission(user, carbonEmissionAmount, goodChoice);
            emissionService.addUserDistance(user, request.getTransportation(), request.getDistanceTravelled());

            return ResponseEntity.ok().build();
        } catch (UserNotFoundException | VehicleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/api/getUserEmission/{id}")
    public Emission getUserEmissionData(@PathVariable Long id) throws UserNotFoundException {
        User user = userService.getUserById(id);
        return user.getEmission();
    }
}
