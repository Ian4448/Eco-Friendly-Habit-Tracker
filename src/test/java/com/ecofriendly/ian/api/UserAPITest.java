package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.*;
import com.ecofriendly.ian.model.enums.TransportationType;
import com.ecofriendly.ian.model.enums.UserRole;
import com.ecofriendly.ian.service.EmissionService;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAPITest {
    @Mock
    private UserService userService;

    @Mock
    private EmissionService emissionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserAPI userAPI;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {}

    @Test
    void addUser_ShouldReturnSavedUser_WhenSuccessful() {
        // Arrange
        Emission emission = new Emission();
        User inputUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), emission, UserRole.USER);
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
        User inputUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), emission, UserRole.USER);
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
        User inputUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), emission, UserRole.USER);
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
        User currentUser = new User("current@test.com", "Current", "User", new ArrayList<>(), new Emission(), UserRole.USER);
        currentUser.setId(1L);

        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission(), UserRole.USER);
        User expectedUser = new User("current@test.com", "Updated", "User", new ArrayList<>(), new Emission(), UserRole.USER);
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
        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission(), UserRole.USER);

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
        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission(), UserRole.USER);

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
        User currentUser = new User("current@test.com", "Current", "User", new ArrayList<>(), new Emission(), UserRole.USER);
        currentUser.setId(1L);

        User updatedUserDetails = new User(null, "Updated", "User", new ArrayList<>(), new Emission(), UserRole.USER);

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
                new User("user1@test.com", "User", "One", new ArrayList<>(), new Emission(), UserRole.USER),
                new User("user2@test.com", "User", "Two", new ArrayList<>(), new Emission(), UserRole.USER)
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
    void deleteUser_ShouldDeleteUser_WhenUserExists() throws UserNotFoundException {
        // Arrange
        String email = "test@example.com";
        User userToDelete = new User(email, "Test", "User", new ArrayList<>(), new Emission(), UserRole.USER);
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
    void getUserById_ShouldReturnUserDTO_WhenUserExists() throws UserNotFoundException {
        // Arrange
        Long userId = 1L;
        User user = new User("test@example.com", "Test", "User", new ArrayList<>(), new Emission(), UserRole.USER);
        user.setId(userId);

        when(userService.getUserById(userId)).thenReturn(user);

        // Act
        ResponseEntity<?> response = userAPI.getUserById(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> userDto = (Map<String, Object>) response.getBody();
        assertEquals(userId, userDto.get("id"));
        assertEquals("test@example.com", userDto.get("email"));
        assertEquals("Test", userDto.get("firstName"));
        assertEquals("User", userDto.get("lastName"));

        verify(userService).getUserById(userId);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws UserNotFoundException {
        // Arrange
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        ResponseEntity<?> response = userAPI.getUserById(userId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("User not found", errorResponse.get("message"));

        verify(userService).getUserById(userId);
    }

    @Test
    void logout_ShouldInvalidateSessionAndClearCookies() {
        // Arrange
        Cookie authCookie = new Cookie("auth_token", "test-token");
        Cookie otherCookie = new Cookie("other_cookie", "other-value");
        Cookie[] cookies = {authCookie, otherCookie};

        when(request.getCookies()).thenReturn(cookies);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> result = userAPI.logout(request, response, session);

        // Assert
        verify(session).invalidate();
        verify(response).addCookie(argThat(cookie ->
                cookie.getName().equals("auth_token") &&
                        cookie.getMaxAge() == 0 &&
                        cookie.getPath().equals("/")
        ));
        assertEquals(HttpStatus.FOUND, result.getStatusCode());
        assertEquals(URI.create("/"), result.getHeaders().getLocation());
    }

    @Test
    void logUserEmissionAndDistanceCount_ShouldUpdateEmissionData_WhenSuccessful() throws UserNotFoundException {
        // Arrange
        Long userId = 1L;
        String vehicleName = "Test Car";
        double distance = 100.0;

        EmissionRequest request = new EmissionRequest();
        request.setUserId(userId);
        request.setVehicleName(vehicleName);
        request.setDistanceTravelled(distance);
        request.setTransportation(TransportationType.CAR);

        User user = new User("test@example.com", "Test", "User", new ArrayList<>(), new Emission(), UserRole.USER);
        Vehicle vehicle = new Vehicle();
        vehicle.setName(vehicleName);
        user.setVehicles(Collections.singletonList(vehicle));

        when(userService.getUserById(userId)).thenReturn(user);
        when(emissionService.calculateCarbonEmission(any(Vehicle.class), anyDouble())).thenReturn(50.0);
        doNothing().when(emissionService).addUserCarbonEmission(any(User.class), anyDouble(), anyBoolean());
        doNothing().when(emissionService).addUserDistance(any(User.class), any(TransportationType.class), anyDouble());

        // Act
        ResponseEntity<Object> response = userAPI.logUserEmissionAndDistanceCount(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).getUserById(userId);
        verify(emissionService).calculateCarbonEmission(eq(vehicle), eq(distance));
        verify(emissionService).addUserCarbonEmission(eq(user), eq(50.0), eq(false));
        verify(emissionService).addUserDistance(eq(user), eq(TransportationType.CAR), eq(distance));
    }

    @Test
    void logUserEmissionAndDistanceCount_ShouldReturnNotFound_WhenUserNotFound() throws UserNotFoundException {
        // Arrange
        EmissionRequest request = new EmissionRequest();
        request.setUserId(1L);
        request.setVehicleName("Test Car");

        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        ResponseEntity<Object> response = userAPI.logUserEmissionAndDistanceCount(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void logUserEmissionAndDistanceCount_ShouldReturnNotFound_WhenVehicleNotFound() throws UserNotFoundException {
        // Arrange
        EmissionRequest request = new EmissionRequest();
        request.setUserId(1L);
        request.setVehicleName("Nonexistent Car");

        User user = new User("test@example.com", "Test", "User", new ArrayList<>(), new Emission(), UserRole.USER);
        when(userService.getUserById(1L)).thenReturn(user);

        // Act
        ResponseEntity<Object> response = userAPI.logUserEmissionAndDistanceCount(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUserEmissionData_ShouldReturnEmissionData_WhenUserExists() throws UserNotFoundException {
        // Arrange
        Long userId = 1L;
        User user = new User("test@example.com", "Test", "User", new ArrayList<>(), new Emission(), UserRole.USER);
        Emission emission = new Emission();
        user.setEmission(emission);

        when(userService.getUserById(userId)).thenReturn(user);

        // Act
        Emission result = userAPI.getUserEmissionData(userId);

        // Assert
        assertNotNull(result);
        assertEquals(emission, result);
        verify(userService).getUserById(userId);
    }

    @Test
    void getUserEmissionData_ShouldThrowException_WhenUserNotFound() throws UserNotFoundException {
        // Arrange
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userAPI.getUserEmissionData(userId));
        verify(userService).getUserById(userId);
    }
}