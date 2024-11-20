package com.ecofriendly.ian.service;

import com.ecofriendly.ian.dao.TokenDAO;
import com.ecofriendly.ian.dao.UserDAO;
import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.Emission;
import com.ecofriendly.ian.model.Token;
import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
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
        user = new User("emailfortesting@example.com","firstNameTest", "lastNameTest", new ArrayList<>(), new Emission());
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

        User updatedDetails = new User(oldUser.getEmail(), "newFirstName", "newLastName", new ArrayList<>(), new Emission());
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

    @Test
    public void testCalculateCarbonEmission() {
        //Todo
    }
}
