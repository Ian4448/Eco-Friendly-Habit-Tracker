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
public class Emission {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID emissionId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    // Daily attributes
    @Column(columnDefinition = "double default 0.0")
    private double dailyEmissionCount = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double dailyDistanceTravelled = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double dailyDistanceWalked = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double dailyDistanceDriven = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double dailyDistanceBiked = 0.0;

    // Weekly attributes
    @Column(columnDefinition = "double default 0.0")
    private double weeklyEmissionCount = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double weeklyDistanceTravelled = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double weeklyDistanceDriven = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double weeklyDistanceWalked = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double weeklyDistanceBiked = 0.0;

    // Monthly attributes
    @Column(columnDefinition = "double default 0.0")
    private double monthlyEmissionCount = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double monthlyDistanceTravelled = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double monthlyDistanceWalked = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double monthlyDistanceDriven = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double monthlyDistanceBiked = 0.0;

    // Total attributes
    @Column(columnDefinition = "double default 0.0")
    private double totalEmissionCount = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double totalDistanceTravelled = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double totalDistanceDriven = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double totalDistanceWalked = 0.0;

    @Column(columnDefinition = "double default 0.0")
    private double totalDistanceBiked = 0.0;

    public Emission() {

    }

    public Emission(User user) {
        this.user = user;
    }
}
