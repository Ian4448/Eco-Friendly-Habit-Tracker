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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Vehicle> vehicles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Emission emission;

    public User(String email, String firstName, String lastName, List<Vehicle> vehicles, Emission emission) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.vehicles = vehicles;
        this.emission = emission;
    }

    public User() {}
}
