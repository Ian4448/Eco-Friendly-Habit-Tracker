package com.ecofriendly.ian.model;

import com.ecofriendly.ian.model.enums.TrackingPeriod;
import com.ecofriendly.ian.model.enums.TransportationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmissionRequest {
    private TrackingPeriod time;
    private TransportationType transportation;
    private Long userId;
    private String vehicleName;
    private double distanceTravelled;
    private boolean goodChoice;
}
