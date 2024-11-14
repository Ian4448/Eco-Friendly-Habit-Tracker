// UserDAO.java
package com.ecofriendly.ian.dao;

import com.ecofriendly.ian.model.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Override
    @NonNull
    List<User> findAll();
}
