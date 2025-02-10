package com.ecofriendly.ian.service;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.exceptions.VehicleNotFoundException;
import com.ecofriendly.ian.model.Emission;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.dao.VehicleDAO;
import com.ecofriendly.ian.exceptions.InvalidVehicleException;
import com.ecofriendly.ian.model.Vehicle;
import com.ecofriendly.ian.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {
    @Mock
    private UserDAO userDAO;

    @Mock
    private VehicleDAO vehicleDAO;

    @InjectMocks
    private VehicleService vehicleService;

    private User user;
    private Vehicle vehicle;
    private UUID vehicleId;

    @BeforeEach
    public void setUp() {
        vehicleId = UUID.randomUUID();
        user = new User("emailfortesting@example.com", "firstNameTest", "lastNameTest", new ArrayList<>(), new Emission(), UserRole.USER);
        vehicle = new Vehicle("make", "model", "name", 45, user);
        vehicle.setVehicleId(vehicleId);
        user.getVehicles().add(vehicle);
    }

    @Test
    public void testGetAllVehicles_UserFound_ReturnsVehicleList() throws UserNotFoundException {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        List<Vehicle> vehicles = vehicleService.getAllVehicles(user.getEmail());

        assertNotNull(vehicles);
        assertEquals(1, vehicles.size());
        assertEquals(vehicle, vehicles.get(0));
        System.out.printf("hello");
    }

    @Test
    public void testGetAllVehicles_UserNotFound_ThrowsUserNotFoundException() {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> vehicleService.getAllVehicles(user.getEmail()));
    }

    @Test
    public void testGetVehicle_VehicleExists_ReturnsVehicle() throws UserNotFoundException, VehicleNotFoundException {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Vehicle foundVehicle = vehicleService.getVehicle(user.getEmail(), vehicleId);

        assertNotNull(foundVehicle);
        assertEquals(vehicleId, foundVehicle.getVehicleId());
    }

    @Test
    public void testGetVehicle_VehicleNotFound_ThrowsVehicleNotFoundException() {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(VehicleNotFoundException.class, () -> vehicleService.getVehicle(user.getEmail(), UUID.randomUUID()));
    }

    @Test
    public void testAddVehicle_UserNotFound_ThrowsUserNotFoundException() {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> vehicleService.addVehicle(vehicle, user.getEmail()));
    }

    @Test
    public void testAddVehicle_VehicleAddedSuccessfully() throws UserNotFoundException, InvalidVehicleException {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(vehicleDAO.save(vehicle)).thenReturn(vehicle);

        Vehicle addedVehicle = vehicleService.addVehicle(vehicle, user.getEmail());

        assertNotNull(addedVehicle);
        assertEquals(vehicle, addedVehicle);
        verify(vehicleDAO, times(1)).save(vehicle);
        verify(userDAO, times(1)).save(user);
    }

    @Test
    public void testUserVehicleCountExceeded_LimitExceeded_ReturnsTrue() throws UserNotFoundException {
        for (int i = 0; i < vehicleService.getMaximumVehiclePerUser(); i++) {
            user.getVehicles().add(new Vehicle());
        }
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertTrue(vehicleService.userVehicleCountExceeded(user.getEmail()));
    }

    @Test
    public void testUserVehicleCountExceeded_LimitNotExceeded_ReturnsFalse() throws UserNotFoundException {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertFalse(vehicleService.userVehicleCountExceeded(user.getEmail()));
    }

    @Test
    public void testDeleteVehicle_VehicleDeletedSuccessfully() throws UserNotFoundException, VehicleNotFoundException {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        vehicleService.deleteVehicle(user.getEmail(), vehicle);

        assertFalse(user.getVehicles().contains(vehicle));
        verify(vehicleDAO, times(1)).delete(vehicle);
        verify(userDAO, times(1)).save(user);
    }
}
