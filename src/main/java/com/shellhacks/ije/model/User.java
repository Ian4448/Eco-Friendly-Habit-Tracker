package com.shellhacks.ije.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class User {
    @Id
    private String email;

    private String firstName;
    private String lastName;

    @OneToMany
    private List<Vehicle> vehicles;

    public User(String email, String firstName, String lastName, List<Vehicle> vehicles) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.vehicles = vehicles;
    }

    public User() {

    }
}
