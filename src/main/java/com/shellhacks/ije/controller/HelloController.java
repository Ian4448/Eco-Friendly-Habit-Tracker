package com.shellhacks.ije.controller;

import com.shellhacks.ije.model.Vehicle;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String hello() {
        Vehicle vehicle = new Vehicle("make", "model");
        return vehicle.getMake();
    }
}
