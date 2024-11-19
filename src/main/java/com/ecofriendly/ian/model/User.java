// User.java
package com.ecofriendly.ian.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    private String email;

    private String firstName;
    private String lastName;
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles;


    private long carbonEmission = 0; // CO2 in KG, defaulted to 0

    public User(String email, String firstName, String lastName, List<Vehicle> vehicles, long carbonEmission) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.vehicles = vehicles;
        this.carbonEmission = carbonEmission;
    }

    public User() {

    }
}
