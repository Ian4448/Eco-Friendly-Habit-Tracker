package com.ecofriendly.ian.controller;

import com.ecofriendly.ian.exceptions.UserNotFoundException;
import com.ecofriendly.ian.model.UserForm;
import com.ecofriendly.ian.service.UserService;
import jakarta.servlet.http.Cookie;
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
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private UserLoginController userLoginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginForm_ShouldRedirectToHome_WhenAuthTokenIsValid() {
        Cookie authCookie = new Cookie("auth_token", "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});
        when(userService.isTokenValid("valid-token")).thenReturn(true);

        String result = userLoginController.loginForm(request, model);

        assertEquals("redirect:/home", result);
        verify(userService, times(1)).isTokenValid("valid-token");
    }

    @Test
    void loginForm_ShouldReturnLoginForm_WhenAuthTokenIsInvalid() {
        Cookie authCookie = new Cookie("auth_token", "invalid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});
        when(userService.isTokenValid("invalid-token")).thenReturn(false);

        String result = userLoginController.loginForm(request, model);

        assertEquals("loginform", result);
        verify(userService, times(1)).isTokenValid("invalid-token");
        verify(model, times(1)).addAttribute(eq("userForm"), any(UserForm.class));
    }

    @Test
    void loginForm_ShouldReturnLoginForm_WhenNoCookiesFound() {
        when(request.getCookies()).thenReturn(null);

        String result = userLoginController.loginForm(request, model);

        assertEquals("loginform", result);
        verify(model, times(1)).addAttribute(eq("userForm"), any(UserForm.class));
    }

    @Test
    void loginPost_ShouldReturnLoginForm_WhenLoginFails() throws UserNotFoundException {
        UserForm userForm = new UserForm("test@example.com", "password", (long) 3);
        when(userService.matchLogin(userForm)).thenReturn(false);

        String result = userLoginController.loginPost(userForm, model, response, session);

        assertEquals("loginform", result);
        verify(model, times(1)).addAttribute("error", "Invalid username or password");
        verify(userService, times(1)).matchLogin(userForm);
    }
}
