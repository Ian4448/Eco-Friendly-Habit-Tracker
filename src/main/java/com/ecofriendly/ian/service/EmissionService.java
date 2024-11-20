package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmissionService {
    private final UserDAO userDAO;
    public EmissionService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Scheduled(cron = "0 0 0 1 * *") // Monthly @ 12 AM of the 1st
    public void resetMonthlyEmissions() {
        List<User> users = userDAO.findAll(); // Fetch all users
        for (User user : users) {
            Emission emission = user.getEmission();
            if (emission != null) {
                emission.setMonthlyEmissionCount(0); // Reset monthly emission count
                emission.setMonthlyDistanceTravelled(0); // Reset monthly distance
                userDAO.save(user); // Save the updated user
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * MON") // Weekly @ 12 AM Monday
    public void resetWeeklyEmissions() {
        List<User> users = userDAO.findAll(); // Fetch all users
        for (User user : users) {
            Emission emission = user.getEmission();
            if (emission != null) {
                emission.setWeeklyEmissionCount(0); // Reset weekly emission count
                emission.setWeeklyDistanceTravelled(0); // Reset weekly distance
                userDAO.save(user); // Save the updated user
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily @ 12 AM
    public void resetDailyEmissions() {
        List<User> users = userDAO.findAll(); // Fetch all users
        for (User user : users) {
            Emission emission = user.getEmission();
            if (emission != null) {
                emission.setDailyEmissionCount(0); // Reset daily emission count
                emission.setDailyDistanceTravelled(0); // Reset daily distance
                userDAO.save(user); // Save the updated user
            }
        }
    }

    /**
     * This method calculates carbon emissions in kilograms for a
     * gasoline-powered vehicle based on the distance traveled and fuel efficiency (mpg).
     * The emission factor of 8.89 kg CO₂ per gallon is a standardized value derived
     * from the carbon content of gasoline and assumes complete combustion.
     */
    public double calculateCarbonEmission(Vehicle vehicle, double distance) {
        return (distance / vehicle.getMpg()) * 8.89;
    }

    /**
     * Modifies the carbon emission total for a specified tracking period (e.g., daily, weekly, monthly, or total)
     * for the given user. This method adjusts the emission count by either adding or subtracting an incoming
     * emission amount based on whether the change is considered eco-friendly or not.
     *
     * <p>The ternary operator is used to determine whether to add or subtract the incoming emission amount:
     * <ul>
     *     <li>If {@code ecoFriendlyChange} is true, the {@code incomingEmissionAmount} is added to the current emission total.</li>
     *     <li>If {@code ecoFriendlyChange} is false, the {@code incomingEmissionAmount} is subtracted from the current emission total.</li>
     * </ul>
     * </p>
     *
     * @param trackingPeriod the {@link TrackingPeriod} indicating which emission total to modify
     *                       (DAILY, WEEKLY, MONTHLY, or TOTAL).
     * @param user the {@link User} whose emission total is to be modified.
     *             The user must have an associated {@link Emission} object.
     * @param incomingEmissionAmount the amount by which to adjust the emission total.
     * @param ecoFriendlyChange a boolean flag indicating the nature of the change:
     *                          {@code true} to add the amount (eco-friendly activity),
     *                          {@code false} to subtract the amount (non-eco-friendly activity).
     * @throws IllegalArgumentException if the provided tracking period is invalid.
     */
    public void modifyUserCarbonEmissionTotal(TrackingPeriod trackingPeriod, User user, double incomingEmissionAmount, boolean ecoFriendlyChange) {
        Emission emission = user.getEmission();
        double currentEmissionTotal;

        if (emission == null) {
            throw new IllegalArgumentException("User does not have associated emission data.");
        }

        switch (trackingPeriod) {
            case DAILY -> {
                currentEmissionTotal = emission.getDailyEmissionCount();
                emission.setDailyEmissionCount(ecoFriendlyChange ? currentEmissionTotal + incomingEmissionAmount :
                        currentEmissionTotal - incomingEmissionAmount);
            }
            case WEEKLY -> {
                currentEmissionTotal = emission.getWeeklyEmissionCount();
                emission.setWeeklyEmissionCount(ecoFriendlyChange ? currentEmissionTotal + incomingEmissionAmount :
                        currentEmissionTotal - incomingEmissionAmount);
            }
            case MONTHLY -> {
                currentEmissionTotal = emission.getMonthlyEmissionCount();
                emission.setMonthlyEmissionCount(ecoFriendlyChange ? currentEmissionTotal + incomingEmissionAmount :
                        currentEmissionTotal - incomingEmissionAmount);
            }
            case TOTAL -> {
                currentEmissionTotal = emission.getTotalEmissionCount();
                emission.setTotalEmissionCount(ecoFriendlyChange ? currentEmissionTotal + incomingEmissionAmount :
                        currentEmissionTotal - incomingEmissionAmount);
            }
            default -> {
                throw new IllegalArgumentException("Invalid Emission Tracking Period");
            }
        }
    }

    /**
     * Modifies the distance total for a specified tracking period (e.g., daily, weekly, monthly, or total)
     * and transportation type (e.g., WALK, BIKE, CAR) for the given user. This method adjusts the distance
     * by either adding or subtracting an incoming distance amount.
     *
     * <p>The ternary operator is used to determine whether to add or subtract the incoming distance amount:
     * <ul>
     *     <li>If {@code addDistance} is true, the {@code distance} is added to the current distance total.</li>
     *     <li>If {@code addDistance} is false, the {@code distance} is subtracted from the current distance total.</li>
     * </ul>
     * </p>
     *
     * @param trackingPeriod the {@link TrackingPeriod} indicating which distance total to modify
     *                       (DAILY, WEEKLY, MONTHLY, or TOTAL).
     * @param user the {@link User} whose distance total is to be modified.
     *             The user must have an associated {@link Emission} object.
     * @param transportationType the {@link TransportationType} indicating the type of transportation.
     * @param distance the distance by which to adjust the total.
     * @throws IllegalArgumentException if the provided tracking period or transportation type is invalid.
     */
    public void modifyUserDistance(TrackingPeriod trackingPeriod, User user, TransportationType transportationType, double distance) {
        Emission emission = user.getEmission();
        if (emission == null) {
            throw new IllegalArgumentException("User does not have associated emission data.");
        }

        switch (trackingPeriod) {
            case DAILY -> updateDistance(emission, transportationType, distance,"daily");
            case WEEKLY -> updateDistance(emission, transportationType, distance, "weekly");
            case MONTHLY -> updateDistance(emission, transportationType, distance, "monthly");
            case TOTAL -> updateDistance(emission, transportationType, distance, "total");
            default -> throw new IllegalArgumentException("Invalid TrackingPeriod");
        }
    }

    /**
     * Helper method to update distance based on transportation type and tracking period.
     */
    private void updateDistance(Emission emission, TransportationType transportationType, double distance, String periodPrefix) {
        double currentDistance;
        String fieldName;

        switch (transportationType) {
            case WALK -> fieldName = periodPrefix + "DistanceWalked";
            case BIKE -> fieldName = periodPrefix + "DistanceBiked";
            case CAR -> fieldName = periodPrefix + "DistanceDriven";
            default -> throw new IllegalArgumentException("Invalid TransportationType");
        }

        try {
            var field = Emission.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            currentDistance = (double) field.get(emission);
            field.set(emission, currentDistance + distance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to update distance: " + e.getMessage());
        }
    }

}
