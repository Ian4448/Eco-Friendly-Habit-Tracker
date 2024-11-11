package com.shellhacks.ije.service;

import com.shellhacks.ije.dao.UserDAO;
import com.shellhacks.ije.dao.VehicleDAO;
import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.exceptions.VehicleNotFoundException;
import com.shellhacks.ije.model.User;
import com.shellhacks.ije.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class VehicleService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private VehicleDAO vehicleDAO;

    // Get all vehicles for a user by email
    public List<Vehicle> getAllVehicles(String email) throws UserNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return user.getVehicles();
    }

    // Get a specific vehicle for a user
    public Vehicle getVehicle(String email, UUID id) throws VehicleNotFoundException, UserNotFoundException {
        return getAllVehicles(email)
                .stream()
                .filter(vehicle -> vehicle.getVehicleId().equals(id))
                .findFirst()
                .orElseThrow(VehicleNotFoundException::new);
    }

    // Add a vehicle to a user
    public Vehicle addVehicle(Vehicle vehicle, String email) throws UserNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        // Set the user for the vehicle
        vehicle.setUser(user);

        // Save the vehicle to the database
        Vehicle savedVehicle = vehicleDAO.save(vehicle);

        // Add the vehicle to the user's list and save the user
        user.getVehicles().add(savedVehicle);
        userDAO.save(user);

        return savedVehicle;
    }

    // Update an existing vehicle for a user
    public Vehicle updateVehicle(Vehicle updatedVehicle, String email) throws UserNotFoundException, VehicleNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        Vehicle existingVehicle = getVehicle(email, updatedVehicle.getVehicleId());

        // Update the vehicle's properties
        existingVehicle.setMake(updatedVehicle.getMake());
        existingVehicle.setModel(updatedVehicle.getModel());
        existingVehicle.setName(updatedVehicle.getName());
        existingVehicle.setMpg(updatedVehicle.getMpg());

        // Save the updated vehicle
        return vehicleDAO.save(existingVehicle);
    }

    public boolean isVehicleNameUsed(String vehicleName, String email) throws UserNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return user.getVehicles().stream().anyMatch(vehicle -> vehicle.getName().equals(vehicleName));
    }

    public Vehicle getVehicleFromName(String vehicleName, String email) throws UserNotFoundException, VehicleNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return user.getVehicles().stream().filter(vehicle -> vehicle.getName().equals(vehicleName)).findFirst().orElseThrow(() -> new VehicleNotFoundException(vehicleName));
    }

    // Delete a vehicle by its ID
    public void deleteVehicle(String email, Vehicle vehicle) throws UserNotFoundException, VehicleNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        // Remove the vehicle from the user's list
        user.getVehicles().remove(vehicle);

        // Save the user without the removed vehicle
        userDAO.save(user);

        // Delete the vehicle from the database
        vehicleDAO.delete(vehicle);
    }

    // Calculate carbon emissions for a vehicle based on distance
    public double calculateCarbonEmission(Vehicle vehicle, double distance) {
        return (distance / vehicle.getMpg()) * 264.172;
    }
}
