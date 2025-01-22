// Vehicle.java
package com.ecofriendly.ian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
public class Vehicle {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID vehicleId;

    private String make, model, name;
    private int mpg;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    public Vehicle() {}

    public Vehicle(String make, String model, String name, int mpg, User user) {
        this.make = make;
        this.model = model;
        this.name = name;
        this.mpg = mpg;
        this.user = user;
    }
}
