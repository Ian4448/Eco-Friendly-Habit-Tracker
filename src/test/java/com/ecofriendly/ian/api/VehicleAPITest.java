package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.Emission;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.model.Vehicle;
import com.ecofriendly.ian.model.enums.UserRole;
import com.ecofriendly.ian.service.UserService;
import com.ecofriendly.ian.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleAPITest {
    @Mock
    private VehicleService vehicleService;

    @Mock
    private UserService userService;

    @InjectMocks
    private VehicleAPI vehicleAPI;

    private User user;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), new Emission(), UserRole.USER);
        vehicle = new Vehicle("make", "model", "vehicleName", 30, user);
    }

    @Test
    void testAddVehicle_Success() throws Exception {
        String token = "valid_token";
        when(userService.getUserByToken(token)).thenReturn(user);
        when(vehicleService.isVehicleNameUsed(vehicle.getName(), user.getEmail())).thenReturn(false);
        when(vehicleService.userVehicleCountExceeded(user.getEmail())).thenReturn(false);
        when(vehicleService.addVehicle(vehicle, user.getEmail())).thenReturn(vehicle);

        ResponseEntity<?> response = vehicleAPI.addVehicle(token, vehicle);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(vehicle, response.getBody());
    }

    @Test
    void testAddVehicle_VehicleNameConflict() throws Exception {
        String token = "valid_token";
        when(userService.getUserByToken(token)).thenReturn(user);
        when(vehicleService.isVehicleNameUsed(vehicle.getName(), user.getEmail())).thenReturn(true);

        ResponseEntity<?> response = vehicleAPI.addVehicle(token, vehicle);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("message"));
        assertEquals("Vehicle name 'vehicleName' is already in use", ((Map<?, ?>) response.getBody()).get("message"));
    }
    @Test
    void testAddVehicle_VehicleCountExceeded() throws Exception {
        String token = "valid_token";
        when(userService.getUserByToken(token)).thenReturn(user);
        when(vehicleService.isVehicleNameUsed(vehicle.getName(), user.getEmail())).thenReturn(false);
        when(vehicleService.userVehicleCountExceeded(user.getEmail())).thenReturn(true);

        ResponseEntity<?> response = vehicleAPI.addVehicle(token, vehicle);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("message"));

        // Using contains bcz vehicle count might be changed in the future.
        String message = (String) ((Map<?, ?>) response.getBody()).get("message");
        assertTrue(message.contains("Vehicle count cannot be greater than"));
    }


    @Test
    void testAddVehicle_InternalServerError() throws Exception {
        String token = "valid_token";
        when(userService.getUserByToken(token)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = vehicleAPI.addVehicle(token, vehicle);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("message"));
        assertEquals("An error occurred while adding the vehicle", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void testGetVehicles_Success() throws Exception {
        String token = "valid_token";
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(vehicle);
        when(userService.getUserByToken(token)).thenReturn(user);
        when(vehicleService.getAllVehicles(user.getEmail())).thenReturn(vehicles);

        List<Vehicle> response = vehicleAPI.getVehicles(token);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(vehicle, response.get(0));
    }

    @Test
    void testGetVehicles_UserNotFound() throws Exception {
        String token = "invalid_token";
        when(userService.getUserByToken(token)).thenThrow(new UserNotFoundException("User not found"));

        List<Vehicle> response = vehicleAPI.getVehicles(token);

        assertNull(response);
    }

    @Test
    void testDeleteVehicle_Success() throws Exception {
        String token = "valid_token";
        String vehicleName = "vehicleName";
        when(userService.getUserByToken(token)).thenReturn(user);
        when(vehicleService.getVehicleFromName(vehicleName, user.getEmail())).thenReturn(vehicle);

        vehicleAPI.deleteVehicle(token, vehicleName);

        verify(vehicleService, times(1)).deleteVehicle(user.getEmail(), vehicle);
    }
}
