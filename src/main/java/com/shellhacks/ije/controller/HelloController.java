package com.shellhacks.ije.controller;

import com.shellhacks.ije.model.User;
import com.shellhacks.ije.model.Vehicle;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("make", "model"));
        User user = new User("email", "firstName", "lastName",
                vehicles);
        return user.getEmail() + " " + user.getFirstName() + " " + user.getLastName() + vehicles.get(0).getMake();
    }
}
