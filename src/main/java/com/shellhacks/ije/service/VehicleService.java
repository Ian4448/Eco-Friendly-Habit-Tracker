package com.shellhacks.ije.service;

import com.shellhacks.ije.dao.UserDAO;
import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.exceptions.VehicleNotFoundException;
import com.shellhacks.ije.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class VehicleService {
    @Autowired
    private UserDAO userDAO;

    public List<Vehicle> getAllVehicles(String email) throws UserNotFoundException {
        if (userDAO.findByEmail(email).isPresent()) {
            return userDAO.findByEmail(email).get().getVehicles();
        }
        throw new UserNotFoundException(email);
    }

    public Vehicle getVehicle(String email, UUID id) throws VehicleNotFoundException, UserNotFoundException {
        return getAllVehicles(email).
                stream().
                filter(vehicle -> vehicle.getVehicleId() == id).
                findFirst().orElseThrow(VehicleNotFoundException::new);
    }

    public Vehicle addVehicle(Vehicle vehicle, String email) throws UserNotFoundException {
        getAllVehicles(email).add(vehicle);
        return vehicle;
    }

    public Vehicle updateVehicle(Vehicle vehicle, String email) throws UserNotFoundException {
        for (Vehicle existingVehicle : getAllVehicles(email)) {
            if (vehicle.getVehicleId() == existingVehicle.getVehicleId()) {
                existingVehicle.setVehicleId(vehicle.getVehicleId());
                existingVehicle.setMake(vehicle.getMake());
                existingVehicle.setModel(vehicle.getModel());
                existingVehicle.setMpg(vehicle.getMpg());
                break;
            }
        }
        return vehicle;
    }

    public void deleteVehicle(String email, UUID id) throws UserNotFoundException {
        for (Vehicle vehicle : getAllVehicles(email)) {
            if (vehicle.getVehicleId() == id) {
                getAllVehicles(email).remove(vehicle);
                break;
            }
        }
    }

    public void calculateFuelEfficiency(Vehicle vehicle) {
        //Todo: add fuel efficiency calculation.
    }

}
