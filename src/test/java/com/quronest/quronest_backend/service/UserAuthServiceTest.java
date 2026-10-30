package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.LoginUserDto;
import com.quronest.quronest_backend.dto.TokenPair;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.UserRepository;
import com.quronest.quronest_backend.security.CookieUtils;
import com.quronest.quronest_backend.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private CookieConfig cookieConfig;
    private CookieUtils cookieUtils;
    private UserAuthService userAuthService;

    private User testUser;

    @BeforeEach
    void setUp() {
        cookieConfig = new CookieConfig();
        cookieUtils = new CookieUtils(cookieConfig);
        userAuthService = new UserAuthService(userService, authenticationManager, tokenService,
                                              userRepository, cookieConfig, cookieUtils, refreshTokenService);

        testUser = new User("Alice", "alice@example.com");
    }

    @Test
    void userLogin_createsTokenPairAndRefreshTokenEntry() {
        LoginUserDto dto = new LoginUserDto();
        dto.setEmail("alice@example.com");
        dto.setPassword("Secret123!");

        when(userRepository.findByEmailAndBlacklistedFalse("alice@example.com")).thenReturn(testUser);
        when(tokenService.generateTokenPair(any(UserDetails.class)))
                .thenReturn(new TokenPair("mock-access", "mock-refresh"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        BooleanDto result = userAuthService.userLogin(dto, null, response);

        assertThat(result.isStatus()).isTrue();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).generateTokenPair(any(UserDetails.class));
        verify(refreshTokenService).createRefreshToken(eq(testUser), eq("mock-refresh"));

        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(c -> assertThat(c).contains("accessToken=mock-access"))
                .anySatisfy(c -> assertThat(c).contains("refreshToken=mock-refresh"));
    }
}
