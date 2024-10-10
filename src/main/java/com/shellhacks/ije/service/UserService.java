// UserService.java
package com.shellhacks.ije.service;

import com.shellhacks.ije.dao.TokenDAO;
import com.shellhacks.ije.dao.UserDAO;
import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.model.Token;
import com.shellhacks.ije.model.User;
import com.shellhacks.ije.model.UserForm;
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

        // Update fields
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setPassword(userDetails.getPassword());

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
}
