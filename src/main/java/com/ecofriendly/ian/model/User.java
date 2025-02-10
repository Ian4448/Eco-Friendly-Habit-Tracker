// User.java
package com.ecofriendly.ian.model;

import com.ecofriendly.ian.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Vehicle> vehicles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Emission emission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    public User(String email, String firstName, String lastName, List<Vehicle> vehicles, Emission emission, UserRole role) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.vehicles = vehicles;
        this.emission = emission;
        this.role = role;
    }

    public User() {}
}
