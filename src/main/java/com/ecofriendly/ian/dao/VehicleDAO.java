package com.ecofriendly.ian.dao;

import com.ecofriendly.ian.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleDAO extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findById(UUID id);

    // Retrieve all vehicles belonging to a specific user by email
    List<Vehicle> findByUserEmail(String email);
}
