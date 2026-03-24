package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.RegisterUserDto;
import com.quronest.quronest_backend.service.UserService;
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

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public BooleanDto registerUserByEmail(Authentication authentication,
                                          @Valid @RequestBody RegisterUserDto registerUserDto) {
        return userService.registerNewUser(registerUserDto, authentication);
    }
}
