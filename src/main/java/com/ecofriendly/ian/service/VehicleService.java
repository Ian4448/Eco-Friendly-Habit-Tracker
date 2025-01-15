package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.exceptions.VehicleNotFoundException;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.dao.VehicleDAO;
import com.ecofriendly.ian.exceptions.InvalidVehicleException;
import com.ecofriendly.ian.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class VehicleService {
    private final static int MAXIMUM_VEHICLE_PER_USER = 10;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private VehicleDAO vehicleDAO;

    public List<Vehicle> getAllVehicles(String email) throws UserNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return user.getVehicles();
    }

    public Vehicle getVehicle(String email, UUID id) throws VehicleNotFoundException, UserNotFoundException {
        return getAllVehicles(email)
                .stream()
                .filter(vehicle -> vehicle.getVehicleId().equals(id))
                .findFirst()
                .orElseThrow(VehicleNotFoundException::new);
    }

    public Vehicle addVehicle(Vehicle vehicle, String email) throws UserNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        vehicle.setUser(user);

        Vehicle savedVehicle = vehicleDAO.save(vehicle);

        user.getVehicles().add(savedVehicle);
        userDAO.save(user);

        return savedVehicle;
    }

    public int getMaximumVehiclePerUser() {
        return MAXIMUM_VEHICLE_PER_USER;
    }

    public boolean userVehicleCountExceeded(String email) throws UserNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return user.getVehicles().size() >= MAXIMUM_VEHICLE_PER_USER;
    }

    public Vehicle updateVehicle(Vehicle updatedVehicle, String email) throws UserNotFoundException, VehicleNotFoundException {
        Vehicle existingVehicle = getVehicle(email, updatedVehicle.getVehicleId());

        existingVehicle.setMake(updatedVehicle.getMake());
        existingVehicle.setModel(updatedVehicle.getModel());
        existingVehicle.setName(updatedVehicle.getName());
        existingVehicle.setMpg(updatedVehicle.getMpg());

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

    public void deleteVehicle(String email, Vehicle vehicle) throws UserNotFoundException {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        user.getVehicles().remove(vehicle);

        userDAO.save(user);

        vehicleDAO.delete(vehicle);
    }
}
