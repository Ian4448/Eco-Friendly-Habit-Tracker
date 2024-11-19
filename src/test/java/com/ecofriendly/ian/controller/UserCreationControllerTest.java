package com.ecofriendly.ian.controller;

import com.ecofriendly.ian.model.User;
import com.ecofriendly.ian.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreationControllerTest {

    @InjectMocks
    private UserCreationController userCreationController;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setFirstName("<script>alert('hack')</script>");
        user.setLastName("<b>Smith</b>");
        user.setEmail("example@<invalid>.com");
        user.setPassword("password123");

        String viewName = userCreationController.createUser(user, model);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).addUser(userCaptor.capture());
        User sanitizedUser = userCaptor.getValue();

        assertEquals("signuppage", viewName);
        assertEquals(Jsoup.clean("<script>alert('hack')</script>", Safelist.none()), sanitizedUser.getFirstName());
        assertEquals(Jsoup.clean("<b>Smith</b>", Safelist.none()), sanitizedUser.getLastName());
        assertEquals(Jsoup.clean("example@<invalid>.com", Safelist.none()), sanitizedUser.getEmail());
        verify(model).addAttribute(eq("user"), any(User.class));
    }

    @Test
    void testShowCreateForm() {
        String viewName = userCreationController.showCreateForm(model);

        assertEquals("signuppage", viewName);
        verify(model).addAttribute(eq("user"), any(User.class));
    }

    @Test
    void testPasswordEncoding() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        userCreationController.createUser(user, model);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).addUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotEquals("password123", savedUser.getPassword()); // Ensure password is hashed
        assertTrue(savedUser.getPassword().startsWith("$2a$")); // BCrypt-specific hash prefix
    }
}
