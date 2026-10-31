package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.LoginUserDto;
import com.quronest.quronest_backend.dto.RegisterUserDto;
import com.quronest.quronest_backend.service.UserAuthService;
import com.quronest.quronest_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserAuthService userAuthService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userService, userAuthService);
    }

    @Test
    void registerUserByEmail_delegatesToUserService() {
        RegisterUserDto dto = new RegisterUserDto();
        Authentication auth = mock(Authentication.class);
        when(userService.registerNewUser(dto, auth)).thenReturn(new BooleanDto(true));

        BooleanDto result = authController.registerUserByEmail(auth, dto);

        assertThat(result.isStatus()).isTrue();
        verify(userService).registerNewUser(dto, auth);
    }

    @Test
    void loginUserByEmail_delegatesToUserAuthService() {
        LoginUserDto dto = new LoginUserDto();
        Authentication auth = mock(Authentication.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(userAuthService.userLogin(dto, auth, response)).thenReturn(new BooleanDto(true));

        BooleanDto result = authController.loginUserByEmail(auth, response, dto);

        assertThat(result.isStatus()).isTrue();
        verify(userAuthService).userLogin(dto, auth, response);
    }

    @Test
    void logoutAuthenticatedUser_delegatesToUserAuthService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(userAuthService.logoutUser(request, response)).thenReturn(new BooleanDto(true));

        BooleanDto result = authController.logoutAuthenticatedUser(request, response);

        assertThat(result.isStatus()).isTrue();
        verify(userAuthService).logoutUser(request, response);
    }
}
