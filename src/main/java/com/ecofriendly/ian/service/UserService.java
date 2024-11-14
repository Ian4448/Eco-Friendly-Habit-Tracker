// UserService.java
package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.TokenDAO;
import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.Token;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.model.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class UserService {
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private TokenDAO tokenDAO;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User addUser(User user) {
        user.setVehicles(new CopyOnWriteArrayList<>()); // empty list of vehicles
        return userDAO.save(user);
    }

    public void deleteUser(User user) {
        userDAO.delete(user);
    }

    public User getUserByEmail(String email) throws UserNotFoundException {
        return userDAO.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    // Update user method
    public User updateUser(String email, User userDetails) throws UserNotFoundException {
        User existingUser = getUserByEmail(email); // Retrieve existing user by email

        if (!existingUser.getEmail().equals(userDetails.getEmail())) {
            deleteUser(existingUser);
        }
        // Update fields
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setPassword(userDetails.getPassword());
        existingUser.setVehicles(userDetails.getVehicles());

        // Save the updated user
        return userDAO.save(existingUser);
    }

    public boolean matchLogin(UserForm userForm) throws UserNotFoundException {
        String encodedPassword = getUserByEmail(userForm.getUsername()).getPassword();
        return encoder.matches(userForm.getPassword(), encodedPassword);
    }

    public Token storeToken(String email, String tokenStr) throws UserNotFoundException {
        User user = getUserByEmail(email);
        Token token = new Token(tokenStr, user, LocalDateTime.now().plusDays(30));
        tokenDAO.save(token);
        return token;
    }

    public boolean isTokenValid(String tokenStr) {
        Optional<Token> token = tokenDAO.findByToken(tokenStr);
        return token.isPresent() && token.get().getExpirationDate().isAfter(LocalDateTime.now());
    }

    public String getEmailFromToken(String tokenStr) throws UserNotFoundException {
        // Find the token in the database
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
