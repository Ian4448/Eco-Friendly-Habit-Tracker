package com.ecofriendly.ian.controller;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.exceptions.InvalidVehicleException;
import com.ecofriendly.ian.model.Vehicle;
import com.ecofriendly.ian.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/createVehicle")
    public String createVehicle(@RequestBody User user, @RequestBody Vehicle vehicle, Model model) throws UserNotFoundException, InvalidVehicleException {
        vehicleService.addVehicle(vehicle, user.getEmail());
        model.addAttribute("vehicle", new Vehicle());
        return "vehicleaddpage";
    }

    @GetMapping("/createVehicle")
    public String showCreateForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "vehicleaddpage";
    }
}
