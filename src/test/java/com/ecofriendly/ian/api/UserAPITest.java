package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.service.EmissionService;
import com.ecofriendly.ian.service.UserService;
import com.ecofriendly.ian.service.VehicleService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserAPITest {
    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserAPI userAPI;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        // Additional setup if needed
    }

    @Test
    void addUser_ShouldReturnSavedUser_WhenSuccessful() {
        // Arrange
        Emission emission = new Emission();
        User inputUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), emission);
        lenient().when(userService.addUser(userCaptor.capture())).thenAnswer(invocation -> {
            User capturedUser = userCaptor.getValue();
            capturedUser.setId(1L);
            return capturedUser;
        });

        // Act
        User result = userAPI.addUser(inputUser);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(1L, result.getId());
        verify(userService).addUser(argThat(user ->
                user.getEmail().equals("test@example.com") &&
                        user.getFirstName().equals("FirstName") &&
                        user.getLastName().equals("LastName")
        ));
    }

    @Test
    void addUser_ShouldReturnNullUser_WhenUserServiceFails() {
        // Arrange
        Emission emission = new Emission();
        User inputUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), emission);
        lenient().when(userService.addUser(userCaptor.capture())).thenReturn(null);

        // Act
        User result = userAPI.addUser(inputUser);

        // Assert
        assertNull(result);
        verify(userService).addUser(argThat(user ->
                user.getEmail().equals("test@example.com")
        ));
    }

    @Test
    void addUser_ShouldHandleException_WhenUserServiceThrowsException() {
        // Arrange
        Emission emission = new Emission();
        User inputUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), emission);
        lenient().when(userService.addUser(userCaptor.capture())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userAPI.addUser(inputUser),
                "Expected addUser to throw RuntimeException"
        );

        assertEquals("Database error", exception.getMessage());
        verify(userService).addUser(argThat(user ->
                user.getEmail().equals("test@example.com")
        ));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser_WhenSuccessful() throws UserNotFoundException {
        // Arrange
        String userId = "1";
        User currentUser = new User("current@test.com", "Current", "User", new ArrayList<>(), new Emission());
        currentUser.setId(1L);

        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission());
        User expectedUser = new User("current@test.com", "Updated", "User", new ArrayList<>(), new Emission());
        expectedUser.setId(1L);

        when(userService.getUserById(1L)).thenReturn(currentUser);
        when(userService.updateUser(eq(currentUser.getEmail()), any(User.class))).thenReturn(expectedUser);

        // Act
        User result = userAPI.updateUser(updatedUserDetails, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("current@test.com", result.getEmail());
        verify(userService).getUserById(1L);
        verify(userService).updateUser(eq(currentUser.getEmail()), any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() throws UserNotFoundException {
        // Arrange
        String userId = "1";
        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission());

        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("User not found with ID: 1"));

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userAPI.updateUser(updatedUserDetails, userId)
        );

        assertEquals("User not found with ID: 1", exception.getMessage());
        verify(userService).getUserById(1L);
        verify(userService, never()).updateUser(anyString(), any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenInvalidUserId() throws UserNotFoundException {
        // Arrange
        String userId = "invalid";
        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission());

        // Act & Assert
        NumberFormatException exception = assertThrows(
                NumberFormatException.class,
                () -> userAPI.updateUser(updatedUserDetails, userId)
        );

        verify(userService, never()).getUserById(anyLong());
        verify(userService, never()).updateUser(anyString(), any(User.class));
    }

    @Test
    void updateUser_ShouldHandleUpdateFailure() throws UserNotFoundException {
        // Arrange
        String userId = "1";
        User currentUser = new User("current@test.com", "Current", "User", new ArrayList<>(), new Emission());
        currentUser.setId(1L);

        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission());

        when(userService.getUserById(1L)).thenReturn(currentUser);
        when(userService.updateUser(eq(currentUser.getEmail()), any(User.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userAPI.updateUser(updatedUserDetails, userId)
        );

        assertEquals("Update failed", exception.getMessage());
        verify(userService).getUserById(1L);
        verify(userService).updateUser(eq(currentUser.getEmail()), any(User.class));
    }

    @Test
    void getUsers_ShouldReturnAllUsers_WhenSuccessful() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(
                new User("user1@test.com", "User", "One", new ArrayList<>(), new Emission()),
                new User("user2@test.com", "User", "Two", new ArrayList<>(), new Emission())
        );
        when(userService.getAllUsers()).thenReturn(expectedUsers);

        // Act
        List<User> result = userAPI.getUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1@test.com", result.get(0).getEmail());
        assertEquals("user2@test.com", result.get(1).getEmail());
        verify(userService).getAllUsers();
    }

    @Test
    void getUsers_ShouldReturnEmptyList_WhenNoUsers() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userAPI.getUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userService).getAllUsers();
    }

    @Test
    void getUser_ShouldReturnUser_WhenUserExists() throws UserNotFoundException {
        // Arrange
        String email = "test@example.com";
        User expectedUser = new User(email, "Test", "User", new ArrayList<>(), new Emission());
        when(userService.getUserByEmail(email)).thenReturn(expectedUser);

        // Act
        User result = userAPI.getUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        verify(userService).getUserByEmail(email);
    }

    @Test
    void getUser_ShouldReturnEmptyUser_WhenUserNotFound() throws UserNotFoundException {
        // Arrange
        String email = "nonexistent@example.com";
        when(userService.getUserByEmail(email)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        User result = userAPI.getUser(email);

        // Assert
        assertNotNull(result);
        assertNull(result.getEmail());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        verify(userService).getUserByEmail(email);
    }

    @Test
    void getUser_ShouldReturnEmptyUser_WhenEmailIsNull() throws UserNotFoundException {
        // Arrange
        when(userService.getUserByEmail(null)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        User result = userAPI.getUser(null);

        // Assert
        assertNotNull(result);
        assertNull(result.getEmail());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        verify(userService).getUserByEmail(null);
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() throws UserNotFoundException {
        // Arrange
        String email = "test@example.com";
        User userToDelete = new User(email, "Test", "User", new ArrayList<>(), new Emission());
        when(userService.getUserByEmail(email)).thenReturn(userToDelete);
        doNothing().when(userService).deleteUser(userToDelete);

        // Act
        userAPI.deleteUser(email);

        // Assert
        verify(userService).getUserByEmail(email);
        verify(userService).deleteUser(userToDelete);
    }

    @Test
    void deleteUser_ShouldHandleNonExistentUser() throws UserNotFoundException {
        // Arrange
        String email = "nonexistent@example.com";
        when(userService.getUserByEmail(email)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        userAPI.deleteUser(email);

        // Assert
        verify(userService).getUserByEmail(email);
        verify(userService, never()).deleteUser(any(User.class));
    }

    @Test
    void getCurrentUser_ShouldReturnEmailFromSession_WhenSessionExists() {
        // Arrange
        String expectedEmail = "test@example.com";
        when(session.getAttribute("userEmail")).thenReturn(expectedEmail);

        // Act
        Map<String, String> result = userAPI.getCurrentUser(session, request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEmail, result.get("email"));
        verify(session).getAttribute("userEmail");
        verify(request, never()).getCookies();
    }

    @Test
    void getCurrentUser_ShouldReturnEmailFromCookie_WhenNoSessionAndCookieExists() {
        // Arrange
        when(session.getAttribute("userEmail")).thenReturn(null);

        Cookie userEmailCookie = new Cookie("user_email", "test@example.com");
        Cookie otherCookie = new Cookie("other_cookie", "other_value");
        Cookie[] cookies = new Cookie[]{userEmailCookie, otherCookie};

        when(request.getCookies()).thenReturn(cookies);

        // Act
        Map<String, String> result = userAPI.getCurrentUser(session, request);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.get("email"));
        verify(session).getAttribute("userEmail");
        verify(request, times(2)).getCookies();
    }

    @Test
    void getCurrentUser_ShouldReturnNull_WhenNoSessionAndNoCookie() {
        // Arrange
        when(session.getAttribute("userEmail")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // Act
        Map<String, String> result = userAPI.getCurrentUser(session, request);

        // Assert
        assertNotNull(result);
        assertNull(result.get("email"));
        verify(session).getAttribute("userEmail");
        verify(request).getCookies();
    }

    @Test
    void getCurrentUser_ShouldReturnNull_WhenNoSessionAndWrongCookie() {
        // Arrange
        when(session.getAttribute("userEmail")).thenReturn(null);
        Cookie wrongCookie = new Cookie("wrong_cookie", "test@example.com");
        Cookie[] cookies = new Cookie[]{wrongCookie};
        when(request.getCookies()).thenReturn(cookies);

        // Act
        Map<String, String> result = userAPI.getCurrentUser(session, request);

        // Assert
        assertNotNull(result);
        assertNull(result.get("email"));
        verify(session).getAttribute("userEmail");
        verify(request).getCookies();
    }

    // missing tests for getUserById and emission functions
}