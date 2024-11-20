package com.ecofriendly.ian.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmissionRequest {
    private TrackingPeriod time;
    private TransportationType transportation;
    private String userEmail;
    private String vehicleName;
    private double distanceTravelled;
    private boolean goodChoice;
}
