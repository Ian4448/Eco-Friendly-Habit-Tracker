package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.TokenDAO;
import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserDAO userDAO;
    @Mock
    private TokenDAO tokenDAO;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("emailfortesting@example.com", "firstNameTest", "lastNameTest", new ArrayList<>(), new Emission(), UserRole.USER);
        user.setId(1L);
    }

    @Test
    void testFullUserServiceFlow() throws UserNotFoundException {
        //Todo: Implement this test
    }

    @Test
    void testValidToken() {
        // Arrange
        String tokenStr = "valid-token-123";
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        Token validToken = new Token(tokenStr, user, futureDate);

        when(tokenDAO.findByToken(tokenStr)).thenReturn(Optional.of(validToken));

        // Act
        boolean isValid = userService.isTokenValid(tokenStr);

        // Assert
        assertTrue(isValid);
        verify(tokenDAO).findByToken(tokenStr);
    }

    @Test
    void testInvalidToken() {
        // Test case 1: Non-existent token
        when(tokenDAO.findByToken("non-existent-token")).thenReturn(Optional.empty());
        assertFalse(userService.isTokenValid("non-existent-token"));

        // Test case 2: Expired token
        String expiredTokenStr = "expired-token";
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Token expiredToken = new Token(expiredTokenStr, user, pastDate);

        when(tokenDAO.findByToken(expiredTokenStr)).thenReturn(Optional.of(expiredToken));
        assertFalse(userService.isTokenValid(expiredTokenStr));
    }

    @Test
    void testGetEmailFromToken() throws UserNotFoundException {
        // Arrange
        String tokenStr = "valid-token-123";
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        Token validToken = new Token(tokenStr, user, futureDate);

        when(tokenDAO.findByToken(tokenStr)).thenReturn(Optional.of(validToken));

        // Act
        String email = userService.getEmailFromToken(tokenStr);

        // Assert
        assertEquals("emailfortesting@example.com", email);
        verify(tokenDAO).findByToken(tokenStr);
    }

    @Test
    void testMatchLoginAndGetUserId() throws UserNotFoundException {
        //Todo: Implement this test
    }

    @Test
    void testMatchLoginAndGetUserIdWithInvalidCredentials() {
        //Todo: Implement this test
    }

    @Test
    void testCreateUser() {
        // Arrange
        when(userDAO.save(any(User.class))).thenReturn(user);

        // Act
        User newUser = userService.addUser(user);

        // Assert
        assertNotNull(newUser);
        assertEquals(user.getEmail(), newUser.getEmail());
        assertEquals(user.getFirstName(), newUser.getFirstName());
        assertEquals(user.getLastName(), newUser.getLastName());
        assertNotNull(newUser.getVehicles());
        assertNotNull(newUser.getEmission());
        verify(userDAO).save(any(User.class));
    }

    @Test
    void testUpdateUser() throws UserNotFoundException {
        // Arrange
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userDAO.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updatedDetails = new User(user.getEmail(), "newFirstName", "newLastName", new ArrayList<>(), new Emission(), UserRole.USER);

        // Act
        User newUser = userService.updateUser(user.getEmail(), updatedDetails);

        // Assert
        assertNotNull(newUser);
        assertEquals(user.getEmail(), newUser.getEmail());
        assertEquals("newFirstName", newUser.getFirstName());
        assertEquals("newLastName", newUser.getLastName());
        verify(userDAO).save(any(User.class));
    }

    @Test
    void testUpdateUserNotFoundThrowsException() {
        // Arrange
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(user.getEmail(), new User()));
    }

    @Test
    void testDeleteUser() {
        // Act
        userService.deleteUser(user);

        // Assert
        verify(userDAO).delete(user);
    }
}