package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.LoginUserDto;
import com.quronest.quronest_backend.dto.RegisterUserDto;
import com.quronest.quronest_backend.service.UserAuthService;
import com.quronest.quronest_backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Urls.PUBLIC_AUTH_CONTROLLER)
public class AuthController {

    private final UserService userService;
    private final UserAuthService userAuthService;

    public AuthController(UserService userService, UserAuthService userAuthService) {
        this.userService = userService;
        this.userAuthService = userAuthService;
    }

    @PostMapping("/register")
    public BooleanDto registerUserByEmail(Authentication authentication,
                                          @Valid @RequestBody RegisterUserDto registerUserDto) {
        return userService.registerNewUser(registerUserDto, authentication);
    }

    @PostMapping("/login")
    public BooleanDto loginUserByEmail(Authentication authentication, HttpServletResponse response,
                                       @Valid @RequestBody LoginUserDto loginUserDto) {
        return userAuthService.userLogin(loginUserDto, authentication, response);
    }

    @PostMapping("/logout")
    public BooleanDto logoutAuthenticatedUser(HttpServletResponse response) {
        return userAuthService.logoutUser(response);
    }
}
