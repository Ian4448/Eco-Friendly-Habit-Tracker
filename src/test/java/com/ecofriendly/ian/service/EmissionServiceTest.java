package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.model.enums.TrackingPeriod;
import com.ecofriendly.ian.model.enums.TransportationType;
import com.ecofriendly.ian.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmissionServiceTest {
    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private EmissionService emissionService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private Emission testEmission;

    @BeforeEach
    void setUp() {
        testEmission = new Emission();
        testUser = new User("test@example.com", "Test", "User", null, testEmission, UserRole.USER);
    }

    @Test
    void resetMonthlyEmissions_ShouldResetAllUsersMonthlyMetrics() {
        // Arrange
        User user1 = createUserWithEmission(100.0, 200.0);
        User user2 = createUserWithEmission(300.0, 400.0);
        when(userDAO.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        emissionService.resetMonthlyEmissions();

        // Assert
        verify(userDAO, times(2)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();

        for (User user : savedUsers) {
            assertEquals(0, user.getEmission().getMonthlyEmissionCount());
            assertEquals(0, user.getEmission().getMonthlyDistanceTravelled());
        }
    }

    @Test
    void resetWeeklyEmissions_ShouldResetAllUsersWeeklyMetrics() {
        // Arrange
        User user1 = createUserWithEmission(100.0, 200.0);
        User user2 = createUserWithEmission(300.0, 400.0);
        when(userDAO.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        emissionService.resetWeeklyEmissions();

        // Assert
        verify(userDAO, times(2)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();

        for (User user : savedUsers) {
            assertEquals(0, user.getEmission().getWeeklyEmissionCount());
            assertEquals(0, user.getEmission().getWeeklyDistanceTravelled());
        }
    }

    @Test
    void resetDailyEmissions_ShouldResetAllUsersDailyMetrics() {
        // Arrange
        User user1 = createUserWithEmission(100.0, 200.0);
        User user2 = createUserWithEmission(300.0, 400.0);
        when(userDAO.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        emissionService.resetDailyEmissions();

        // Assert
        verify(userDAO, times(2)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();

        for (User user : savedUsers) {
            assertEquals(0, user.getEmission().getDailyEmissionCount());
            assertEquals(0, user.getEmission().getDailyDistanceTravelled());
        }
    }

    @Test
    void calculateCarbonEmission_ShouldCalculateCorrectly() {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setMpg(25); // 25 miles per gallon
        double distance = 100.0; // 100 miles

        // Expected: (100 miles / 25 mpg) * 8.89 kg CO2 per gallon
        double expectedEmission = (distance / vehicle.getMpg()) * 8.89;

        // Act
        double result = emissionService.calculateCarbonEmission(vehicle, distance);

        // Assert
        assertEquals(expectedEmission, result, 0.001);
    }

    @Test
    void addUserCarbonEmission_ShouldAddEmissionForNonEcoFriendlyChoice() {
        // Arrange
        double initialEmission = 100.0;
        testEmission.setDailyEmissionCount(initialEmission);
        testEmission.setWeeklyEmissionCount(initialEmission);
        testEmission.setMonthlyEmissionCount(initialEmission);
        testEmission.setTotalEmissionCount(initialEmission);

        // Act
        emissionService.addUserCarbonEmission(testUser, 50.0, false);

        // Assert
        verify(userDAO).save(testUser);
        assertEquals(150.0, testEmission.getDailyEmissionCount());
        assertEquals(150.0, testEmission.getWeeklyEmissionCount());
        assertEquals(150.0, testEmission.getMonthlyEmissionCount());
        assertEquals(150.0, testEmission.getTotalEmissionCount());
    }

    @Test
    void addUserCarbonEmission_ShouldSubtractEmissionForEcoFriendlyChoice() {
        // Arrange
        double initialEmission = 100.0;
        testEmission.setDailyEmissionCount(initialEmission);
        testEmission.setWeeklyEmissionCount(initialEmission);
        testEmission.setMonthlyEmissionCount(initialEmission);
        testEmission.setTotalEmissionCount(initialEmission);

        // Act
        emissionService.addUserCarbonEmission(testUser, 50.0, true);

        // Assert
        verify(userDAO).save(testUser);
        assertEquals(50.0, testEmission.getDailyEmissionCount());
        assertEquals(50.0, testEmission.getWeeklyEmissionCount());
        assertEquals(50.0, testEmission.getMonthlyEmissionCount());
        assertEquals(50.0, testEmission.getTotalEmissionCount());
    }

    @Test
    void addUserCarbonEmission_ShouldThrowException_WhenEmissionIsNull() {
        // Arrange
        testUser.setEmission(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> emissionService.addUserCarbonEmission(testUser, 50.0, true));
        verify(userDAO, never()).save(any());
    }

    @Test
    void modifyUserCarbonEmissionTotal_ShouldModifyDailyEmission() {
        // Arrange
        testEmission.setDailyEmissionCount(100.0);

        // Act
        emissionService.modifyUserCarbonEmissionTotal(TrackingPeriod.DAILY, testUser, 50.0, true);

        // Assert
        verify(userDAO).save(testUser);
        assertEquals(50.0, testEmission.getDailyEmissionCount());
    }

//    @Test
//    void addUserDistance_ShouldAddCarDistance() {
//        // Arrange
//        double initialDistance = 100.0;
//        testEmission.setDailyDistanceDriven(initialDistance);
//        testEmission.setWeeklyDistanceDriven(initialDistance);
//        testEmission.setMonthlyDistanceDriven(initialDistance);
//        testEmission.setTotalDistanceDriven(initialDistance);
//
//        // Act
//        emissionService.addUserDistance(testUser, TransportationType.CAR, 50.0);
//
//        // Assert
//        verify(userDAO).save(testUser);
//        assertEquals(150.0, testEmission.getDailyDistanceDriven());
//        assertEquals(150.0, testEmission.getWeeklyDistanceDriven());
//        assertEquals(150.0, testEmission.getMonthlyDistanceDriven());
//        assertEquals(150.0, testEmission.getTotalDistanceDriven());
//    } // Todo: Fix logic related to this test

    @Test
    void addUserDistance_ShouldAddWalkDistance() {
        // Arrange
        double initialDistance = 100.0;
        testEmission.setDailyDistanceWalked(initialDistance);
        testEmission.setWeeklyDistanceWalked(initialDistance);
        testEmission.setMonthlyDistanceWalked(initialDistance);
        testEmission.setTotalDistanceWalked(initialDistance);

        // Act
        emissionService.addUserDistance(testUser, TransportationType.WALK, 50.0);

        // Assert
        verify(userDAO).save(testUser);
        assertEquals(150.0, testEmission.getDailyDistanceWalked());
        assertEquals(150.0, testEmission.getWeeklyDistanceWalked());
        assertEquals(150.0, testEmission.getMonthlyDistanceWalked());
        assertEquals(150.0, testEmission.getTotalDistanceWalked());
    }

    private User createUserWithEmission(double emissionCount, double distance) {
        Emission emission = new Emission();
        emission.setMonthlyEmissionCount(emissionCount);
        emission.setWeeklyEmissionCount(emissionCount);
        emission.setDailyEmissionCount(emissionCount);
        emission.setMonthlyDistanceTravelled(distance);
        emission.setWeeklyDistanceTravelled(distance);
        emission.setDailyDistanceTravelled(distance);

        return new User("test@example.com", "Test", "User", null, emission, UserRole.USER);
    }
}