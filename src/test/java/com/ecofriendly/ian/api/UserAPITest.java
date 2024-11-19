package com.ecofriendly.ian.api;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserAPITest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserAPI userAPI;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("test@example.com", "FirstName", "LastName", new ArrayList<>(), 0);
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
}
