package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.exceptions.VehicleNotFoundException;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.model.enums.TransportationType;
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

    @DeleteMapping({"/api/deleteUser", "/api/deleteUser/"})
    public ResponseEntity<?> deleteUser(@RequestParam String email) {
        try {
            User user = userService.getUserByEmail(email);
            userService.deleteUser(user);
            logger.info(String.format("User deleted successfully: {%s}", email));
            return ResponseEntity.ok().build();

        } catch (UserNotFoundException e) {
            logger.info(String.format("Attempt to delete non-existent user: {%s}", email));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
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
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    logger.info("Deleted auth_token cookie");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/"))
                .build();
    }

    /**
     * Updates a user's carbon emission log and distance traveled based on their transportation choice.
     * This endpoint processes an emission request by retrieving the user and their vehicle,
     * calculating the carbon emission for the given distance, and updating the user's emission
     * and distance records accordingly.
     *
     * <p>If the user or vehicle is not found, it returns a 404 NOT FOUND response.
     * If there is any other issue with processing the request, it returns a 400 BAD REQUEST response.</p>
     *
     * @param request the {@link EmissionRequest} containing user ID, vehicle name,
     *                distance traveled, and transportation type.
     * @return {@link ResponseEntity} with status:
     *         <ul>
     *           <li>200 OK if the emission and distance are successfully logged.</li>
     *           <li>404 NOT FOUND if the user or vehicle does not exist.</li>
     *           <li>400 BAD REQUEST for any other processing errors.</li>
     *         </ul>
     */
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
