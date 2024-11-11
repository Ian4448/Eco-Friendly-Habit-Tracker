package com.shellhacks.ije.api;

import com.shellhacks.ije.exceptions.InvalidVehicleException;
import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.model.User;
import com.shellhacks.ije.model.Vehicle;
import com.shellhacks.ije.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class VehicleCreationAPI {
    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/createVehicle")
    public String createVehicle(@RequestBody User user, @RequestBody Vehicle vehicle, Model model) throws UserNotFoundException, InvalidVehicleException {
        vehicleService.addVehicle(vehicle, user.getEmail());
        model.addAttribute("vehicle", new Vehicle());
        return "VehicleAddPage";
    }

    @GetMapping("/createVehicle")
    public String showCreateForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "VehicleAddPage";
    }
}
