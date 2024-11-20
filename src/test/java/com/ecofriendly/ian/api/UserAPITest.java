package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.Emission;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), new Emission());
    }

    @Test
    void addUser_ShouldReturnAddedUser() {
        when(userService.addUser(testUser)).thenReturn(testUser);

        User result = userAPI.addUser(testUser);

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userService, times(1)).addUser(testUser);
    }

    @Test
    void updateUser_ShouldReturnEmptyUser_WhenUserNotFound() throws UserNotFoundException {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).updateUser(testUser.getEmail(), testUser);

        User result = userAPI.updateUser(testUser);

        assertNotNull(result);
        assertNull(result.getEmail());
        verify(userService, times(1)).updateUser(testUser.getEmail(), testUser);
    }

    @Test
    void getUsers_ShouldReturnListOfUsers() {
        List<User> users = List.of(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        List<User> result = userAPI.getUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getEmail(), result.get(0).getEmail());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUser_ShouldReturnUser_WhenUserExists() throws UserNotFoundException {
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(testUser);

        User result = userAPI.getUser(testUser.getEmail());

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userService, times(1)).getUserByEmail(testUser.getEmail());
    }

    @Test
    void getUser_ShouldReturnEmptyUser_WhenUserNotFound() throws UserNotFoundException {
        when(userService.getUserByEmail(testUser.getEmail()))
                .thenThrow(new UserNotFoundException("User not found"));

        User result = userAPI.getUser(testUser.getEmail());

        assertNotNull(result);
        assertNull(result.getEmail());
        verify(userService, times(1)).getUserByEmail(testUser.getEmail());
    }

    @Test
    void deleteUser_ShouldCallDeleteUser_WhenUserExists() throws UserNotFoundException {
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(testUser);
        doNothing().when(userService).deleteUser(testUser);

        userAPI.deleteUser(testUser.getEmail());

        verify(userService, times(1)).getUserByEmail(testUser.getEmail());
        verify(userService, times(1)).deleteUser(testUser);
    }

    @Test
    void deleteUser_ShouldDoNothing_WhenUserNotFound() throws UserNotFoundException {
        when(userService.getUserByEmail(testUser.getEmail()))
                .thenThrow(new UserNotFoundException("User not found"));

        userAPI.deleteUser(testUser.getEmail());

        verify(userService, times(1)).getUserByEmail(testUser.getEmail());
        verify(userService, never()).deleteUser(any());
    }

    @Test
    void getCurrentUser_ShouldReturnEmail_WhenEmailIsInSession() {
        when(session.getAttribute("userEmail")).thenReturn("test@example.com");

        Map<String, String> result = userAPI.getCurrentUser(session, request);

        assertNotNull(result);
        assertEquals("test@example.com", result.get("email"));
        verify(session, times(1)).getAttribute("userEmail");
    }

    @Test
    void getCurrentUser_ShouldReturnEmptyMap_WhenEmailIsNotInSession() {
        when(session.getAttribute("userEmail")).thenReturn(null);

        Map<String, String> result = userAPI.getCurrentUser(session, request);

        assertNotNull(result);
        assertEquals(Collections.singletonMap("email", null), result);
        verify(session, times(1)).getAttribute("userEmail");
    }
}
