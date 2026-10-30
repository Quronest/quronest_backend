package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.LoginUserDto;
import com.quronest.quronest_backend.dto.TokenPair;
import com.quronest.quronest_backend.exception.InvalidLoginCredentialsException;
import com.quronest.quronest_backend.exception.UserNotFoundException;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.UserRepository;
import com.quronest.quronest_backend.security.CookieUtils;
import com.quronest.quronest_backend.security.SecurityUser;
import com.quronest.quronest_backend.security.TokenService;
import com.quronest.quronest_backend.utils.EmailNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthService.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final CookieConfig cookieConfig;
    private final CookieUtils cookieUtils;
    private final RefreshTokenService refreshTokenService;

    public UserAuthService(UserService userService, AuthenticationManager authenticationManager,
                           TokenService tokenService, UserRepository userRepository,
                           CookieConfig cookieConfig, CookieUtils cookieUtils,
                           RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.cookieConfig = cookieConfig;
        this.cookieUtils = cookieUtils;
        this.refreshTokenService = refreshTokenService;
    }

    public BooleanDto userLogin(LoginUserDto loginUserDto, Authentication authentication,
                                HttpServletResponse response) {
        userService.checkUserAlreadyAuthenticated(authentication);

        String normalizedEmail = EmailNormalizer.normalize(loginUserDto.getEmail());
        // check user exists
        User user = userRepository.findByEmailAndBlacklistedFalse(normalizedEmail);
        if (user == null) {
            throw new UserNotFoundException();
        }

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(normalizedEmail,
                                                                                              loginUserDto.getPassword());

        try {
            authenticationManager.authenticate(authReq);
        } catch (AuthenticationException e) {
            throw new InvalidLoginCredentialsException();
        }

        // get principle
        UserDetails principle = new SecurityUser(user);

        // Generate token pair
        TokenPair tokenPair = tokenService.generateTokenPair(principle);

        // Add a fresh refreshToken entry for this session
        refreshTokenService.createRefreshToken(user, tokenPair.getRefreshToken());

        // set cookies
        cookieUtils.addCookie(response, "accessToken", tokenPair.getAccessToken(), cookieConfig.getAccessTokenMaxAge());
        cookieUtils.addCookie(response, "refreshToken", tokenPair.getRefreshToken(), cookieConfig.getRefreshTokenMaxAge());

        return new BooleanDto(true);
    }

    public BooleanDto logoutUser(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenService.revokeRefreshToken(request, response);
        return new BooleanDto(true);
    }

    // this is just an abstraction. Visit CustomLogoutHandler for /logout route
    public BooleanDto logoutUser(HttpServletResponse response) {
        // clear cookies
        cookieUtils.clearCookie(response, "accessToken");
        cookieUtils.clearCookie(response, "refreshToken");

        // remove current auth
        SecurityContextHolder.clearContext();

        return new BooleanDto(true);
    }
}
