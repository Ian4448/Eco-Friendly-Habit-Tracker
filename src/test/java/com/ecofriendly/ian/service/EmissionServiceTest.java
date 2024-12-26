package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.dao.VehicleDAO;
import com.ecofriendly.ian.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmissionServiceTest {
    @Mock
    private UserDAO userDAO;

    @Mock
    private VehicleDAO vehicleDAO;

    @InjectMocks
    private VehicleService vehicleService;

    private User user;
    private Vehicle vehicle;
    private UUID vehicleId;

    @InjectMocks
    private EmissionService emissionService;

    private User testUser;
    private Emission testEmission;

    @BeforeEach
    void setUp() {
        testEmission = new Emission();
        testEmission.setDailyEmissionCount(10);
        testEmission.setWeeklyEmissionCount(50);
        testEmission.setMonthlyEmissionCount(200);
        testEmission.setTotalEmissionCount(500);

        testUser = new User("test@example.com", "John", "Doe", new ArrayList<>(), testEmission);
    }

    @Test
    void resetDailyEmissions_ShouldResetDailyEmissionValues() {
        when(userDAO.findAll()).thenReturn(List.of(testUser));

        emissionService.resetDailyEmissions();

        assertEquals(0, testEmission.getDailyEmissionCount());
        assertEquals(0, testEmission.getDailyDistanceTravelled());
        verify(userDAO).save(testUser);
    }

    @Test
    void resetWeeklyEmissions_ShouldResetWeeklyEmissionValues() {
        when(userDAO.findAll()).thenReturn(List.of(testUser));

        emissionService.resetWeeklyEmissions();

        assertEquals(0, testEmission.getWeeklyEmissionCount());
        assertEquals(0, testEmission.getWeeklyDistanceTravelled());
        verify(userDAO).save(testUser);
    }

    @Test
    void resetMonthlyEmissions_ShouldResetMonthlyEmissionValues() {
        when(userDAO.findAll()).thenReturn(List.of(testUser));

        emissionService.resetMonthlyEmissions();

        assertEquals(0, testEmission.getMonthlyEmissionCount());
        assertEquals(0, testEmission.getMonthlyDistanceTravelled());
        verify(userDAO).save(testUser);
    }

    @Test
    void calculateCarbonEmission_ShouldReturnCorrectEmissionValue() {
        Vehicle testVehicle = new Vehicle("Tesla", "Model 3", "Electric", 30, testUser);
        double distance = 100;

        double result = emissionService.calculateCarbonEmission(testVehicle, distance);

        assertEquals((distance / 30) * 8.89, result, 0.01);
    }

    @Test
    void modifyUserCarbonEmissionTotal_ShouldAddDailyEmissionForEcoFriendlyActivity() {
        double additionalEmission = 5.0;
        emissionService.modifyUserCarbonEmissionTotal(TrackingPeriod.DAILY, testUser, additionalEmission, true);

        assertEquals(15, testEmission.getDailyEmissionCount());
    }

    @Test
    void modifyUserCarbonEmissionTotal_ShouldSubtractWeeklyEmissionForNonEcoFriendlyActivity() {
        double reducedEmission = 10.0;
        emissionService.modifyUserCarbonEmissionTotal(TrackingPeriod.WEEKLY, testUser, reducedEmission, false);

        assertEquals(40, testEmission.getWeeklyEmissionCount());
    }

    @Test
    void modifyUserCarbonEmissionTotal_ShouldThrowExceptionForInvalidTrackingPeriod() {
        assertThrows(NullPointerException.class, () ->
                emissionService.modifyUserCarbonEmissionTotal(null, testUser, 10, true));
    }

    @Test
    void modifyUserCarbonEmissionTotal_ShouldThrowExceptionWhenEmissionIsNull() {
        testUser.setEmission(null);

        assertThrows(IllegalArgumentException.class, () ->
                emissionService.modifyUserCarbonEmissionTotal(TrackingPeriod.DAILY, testUser, 10, true));
    }

//    @Test
//    void modifyUserDistance_ShouldUpdateDailyDistanceWalked() {
//        emissionService.modifyUserDistance(TrackingPeriod.DAILY, testUser, TransportationType.WALK, 5.0);
//
//        assertEquals(5.0, testEmission.getDailyDistanceWalked());
//    }
//
//    @Test
//    void modifyUserDistance_ShouldThrowExceptionForInvalidTransportationType() {
//        assertThrows(NullPointerException.class, () ->
//                emissionService.modifyUserDistance(TrackingPeriod.DAILY, testUser, null, 10.0));
//    }
}

