package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.model.enums.TrackingPeriod;
import com.ecofriendly.ian.model.enums.TransportationType;
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
        List<User> users = userDAO.findAll();
        for (User user : users) {
            Emission emission = user.getEmission();
            if (emission != null) {
                emission.setMonthlyEmissionCount(0);
                emission.setMonthlyDistanceTravelled(0);
                userDAO.save(user);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * MON") // Weekly @ 12 AM Monday
    public void resetWeeklyEmissions() {
        List<User> users = userDAO.findAll();
        for (User user : users) {
            Emission emission = user.getEmission();
            if (emission != null) {
                emission.setWeeklyEmissionCount(0);
                emission.setWeeklyDistanceTravelled(0);
                userDAO.save(user);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily @ 12 AM
    public void resetDailyEmissions() {
        List<User> users = userDAO.findAll();
        for (User user : users) {
            Emission emission = user.getEmission();
            if (emission != null) {
                emission.setDailyEmissionCount(0);
                emission.setDailyDistanceTravelled(0);
                userDAO.save(user);
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
     * Updates the user's carbon emission metrics by adding or subtracting the specified emission amount
     * based on whether the action is considered eco-friendly. This method adjusts the daily, weekly,
     * monthly, and total emission counts in the user's associated `Emission` object and saves the changes
     * to the database.
     *
     * @param user the {@link User} whose emission data is being updated.
     * @param emissionAmount the amount of carbon emissions to add or subtract.
     * @param isGoodChoice a boolean indicating whether the activity is eco-friendly
     *                     (subtracts emissions if true, adds if false).
     * @throws IllegalArgumentException if the user does not have associated emission data.
     */
    public void addUserCarbonEmission(User user, double emissionAmount, boolean isGoodChoice) {
        Emission emission = user.getEmission();

        if (emission == null) {
            throw new IllegalArgumentException("User does not have associated emission data.");
        }

        double amountToAdd = isGoodChoice ? -emissionAmount : emissionAmount;

        emission.setDailyEmissionCount(emission.getDailyEmissionCount() + amountToAdd);
        emission.setWeeklyEmissionCount(emission.getWeeklyEmissionCount() + amountToAdd);
        emission.setMonthlyEmissionCount(emission.getMonthlyEmissionCount() + amountToAdd);
        emission.setTotalEmissionCount(emission.getTotalEmissionCount() + amountToAdd);
        userDAO.save(user);
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
                emission.setDailyEmissionCount(ecoFriendlyChange ? currentEmissionTotal - incomingEmissionAmount :
                        currentEmissionTotal + incomingEmissionAmount);
            }
            case WEEKLY -> {
                currentEmissionTotal = emission.getWeeklyEmissionCount();
                emission.setWeeklyEmissionCount(ecoFriendlyChange ? currentEmissionTotal - incomingEmissionAmount :
                        currentEmissionTotal + incomingEmissionAmount);
            }
            case MONTHLY -> {
                currentEmissionTotal = emission.getMonthlyEmissionCount();
                emission.setMonthlyEmissionCount(ecoFriendlyChange ? currentEmissionTotal - incomingEmissionAmount :
                        currentEmissionTotal + incomingEmissionAmount);
            }
            case TOTAL -> {
                currentEmissionTotal = emission.getTotalEmissionCount();
                System.out.printf("current total %f", currentEmissionTotal);
                emission.setTotalEmissionCount(ecoFriendlyChange ? currentEmissionTotal - incomingEmissionAmount :
                        currentEmissionTotal + incomingEmissionAmount);
                System.out.printf("new total %f", currentEmissionTotal);
            }
            default -> {
                throw new IllegalArgumentException("Invalid Emission Tracking Period");
            }
        }
        userDAO.save(user);
    }

    /**
     * Updates the user's distance metrics for a specified transportation type by adding the given distance
     * to their daily, weekly, monthly, and total records. This method ensures that the associated
     * `Emission` object is updated and persists the changes to the database. It validates that the
     * user has emission data and handles transportation types (CAR, WALK, BIKE) using the
     * `TransportationType` enum. Throws an exception if no emission data is found or the transportation
     * type is invalid.
     *
     * @param user the {@link User} whose distance is being updated.
     * @param transportationType the mode of transportation (e.g., CAR, WALK, BIKE).
     * @param distance the distance to add.
     * @throws IllegalArgumentException if the user has no emission data or the transportation type is invalid.
     */

    public void addUserDistance(User user, TransportationType transportationType, double distance) {
        Emission emission = user.getEmission();
        if (emission == null) {
            throw new IllegalArgumentException("User does not have associated emission data.");
        }

        switch (transportationType) {
            case CAR -> {
                emission.setDailyDistanceDriven(emission.getDailyDistanceDriven() + distance);
                emission.setWeeklyDistanceDriven(emission.getWeeklyDistanceTravelled() + distance);
                emission.setMonthlyDistanceDriven(emission.getMonthlyDistanceDriven() + distance);
                emission.setTotalDistanceDriven(emission.getTotalDistanceDriven() + distance);
            }
            case WALK -> {
                emission.setDailyDistanceWalked(emission.getDailyDistanceWalked() + distance);
                emission.setWeeklyDistanceWalked(emission.getWeeklyDistanceWalked() + distance);
                emission.setMonthlyDistanceWalked(emission.getMonthlyDistanceWalked() + distance);
                emission.setTotalDistanceWalked(emission.getTotalDistanceWalked() + distance);
            }
            case BIKE -> {
                emission.setDailyDistanceBiked(emission.getDailyDistanceBiked() + distance);
                emission.setWeeklyDistanceBiked(emission.getWeeklyDistanceBiked() + distance);
                emission.setMonthlyDistanceBiked(emission.getMonthlyDistanceBiked() + distance);
                emission.setTotalDistanceBiked(emission.getTotalDistanceBiked() + distance);
            }
            default -> throw new IllegalArgumentException("Invalid TransportationType: " + transportationType);
        }
        userDAO.save(user);
    }

}
