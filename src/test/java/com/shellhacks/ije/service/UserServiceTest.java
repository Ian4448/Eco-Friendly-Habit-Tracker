package com.shellhacks.ije.service;

import com.shellhacks.ije.dao.TokenDAO;
import com.shellhacks.ije.dao.UserDAO;
import com.shellhacks.ije.exceptions.UserNotFoundException;
import com.shellhacks.ije.model.Token;
import com.shellhacks.ije.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserDAO userDAO;
    @Mock
    private TokenDAO tokenDAO;

    @InjectMocks
    private UserService userService;

    private User user;
    private Token token;

    @BeforeEach
    public void setUp() {
        user = new User("emailfortesting@example.com","firstNameTest", "lastNameTest", new ArrayList<>());
    }

    @Test
    public void testFullUserServiceFlow() {
        //Todo: 11/13/24
    }

    @Test
    public void testValidToken() {
        //Todo: 11/13/24
    }

    @Test
    public void testInvalidToken() {
        //Todo: 11/13/24
    }

    @Test
    public void testCreateUser() {
        when(userDAO.save(user)).thenReturn(user);

        User newUser = userService.addUser(user);
        assertThat(newUser).isNotNull();

        assertThat(newUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(newUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(newUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(newUser.getPassword()).isEqualTo(user.getPassword());

        verify(userDAO, times(1)).save(user);
    }

    @Test
    public void testUpdateUser() throws UserNotFoundException {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User oldUser = userService.addUser(user);
        assertThat(oldUser).isNotNull();

        User updatedDetails = new User(oldUser.getEmail(), "newFirstName", "newLastName", new ArrayList<>());
        User newUser = userService.updateUser(oldUser.getEmail(), updatedDetails);

        assertThat(newUser).isNotNull();
        assertThat(newUser.getEmail()).isEqualTo(oldUser.getEmail());
        assertThat(newUser.getFirstName()).isEqualTo("newFirstName");
        assertThat(newUser.getLastName()).isEqualTo("newLastName");

        verify(userDAO, times(2)).save(newUser);
    }

    @Test
    public void testUpdateUserNotFoundThrowsException() {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.updateUser(user.getEmail(), new User())
        );
    }


    @Test
    public void testDeleteUser() throws UserNotFoundException {
        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        userService.deleteUser(user);

        verify(userDAO, times(1)).delete(user);

        when(userDAO.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(user.getEmail()));
    }
}
