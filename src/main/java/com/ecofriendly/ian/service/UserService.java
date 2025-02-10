package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.TokenDAO;
import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.model.enums.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class UserService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UserDAO userDAO;
    private final TokenDAO tokenDAO;
    public UserService(UserDAO userDAO, TokenDAO tokenDAO) {
        this.userDAO = userDAO;
        this.tokenDAO = tokenDAO;
    }

    public User addUser(User user) {
        user.setVehicles(new CopyOnWriteArrayList<>());
        user.setEmission(new Emission(user));
        user.setRole(UserRole.USER);
        return userDAO.save(user);
    }

    public void deleteUser(User user) {
        userDAO.delete(user);
    }

    public User getUserByEmail(String email) throws UserNotFoundException {
        return userDAO.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    public User getUserById(Long id) throws UserNotFoundException {
        return userDAO.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public User updateUser(String currentEmail, User userDetails) throws UserNotFoundException {
        User existingUser = getUserByEmail(currentEmail);

        // Update fields if provided in userDetails
        if (userDetails.getEmail() != null) {
            existingUser.setEmail(userDetails.getEmail());
        }
        if (userDetails.getFirstName() != null) {
            existingUser.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            existingUser.setLastName(userDetails.getLastName());
        }
        if (userDetails.getPassword() != null) {
            existingUser.setPassword(encoder.encode(userDetails.getPassword()));
        }
        if (userDetails.getEmission() != null) {
            existingUser.setEmission(userDetails.getEmission());
        }

        // Handle vehicles if present
        if (userDetails.getVehicles() != null) {
            existingUser.getVehicles().clear();
            userDetails.getVehicles().forEach(vehicle -> {
                vehicle.setUser(existingUser);
                existingUser.getVehicles().add(vehicle);
            });
        }

        return userDAO.save(existingUser);
    }

    public boolean matchLogin(UserForm userForm) throws UserNotFoundException {
        String encodedPassword = getUserByEmail(userForm.getUsername()).getPassword();
        return encoder.matches(userForm.getPassword(), encodedPassword);
    }

    public long matchLoginAndGetUserId(UserForm userForm) throws UserNotFoundException {
        User user = getUserByEmail(userForm.getUsername());
        String encodedPassword = user.getPassword();

        if (!encoder.matches(userForm.getPassword(), encodedPassword))
            throw new UserNotFoundException("Invalid User Login");

        return user.getId();
    }

    public Token storeToken(Long userId, String tokenStr) throws UserNotFoundException {
        User user = getUserById(userId);
        Token token = new Token(tokenStr, user, LocalDateTime.now().plusDays(30));
        tokenDAO.save(token);
        return token;
    }

    public boolean isTokenValid(String tokenStr) {
        Optional<Token> token = tokenDAO.findByToken(tokenStr);
        return token.isPresent() && token.get().getExpirationDate().isAfter(LocalDateTime.now());
    }

    public String getEmailFromToken(String tokenStr) throws UserNotFoundException {
        Optional<Token> token = tokenDAO.findByToken(tokenStr);

        if (token.isPresent() && token.get().getExpirationDate().isAfter(LocalDateTime.now())) {
            // Extract the email from the user associated with the token
            return token.get().getUser().getEmail();
        } else {
            throw new UserNotFoundException("Invalid or expired token");
        }
    }

    public User getUserByToken(String token) throws UserNotFoundException {
        String email = getEmailFromToken(token);
        return userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }
}
