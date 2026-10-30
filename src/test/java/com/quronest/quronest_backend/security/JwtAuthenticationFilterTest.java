package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.Constants;
import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private FilterChain filterChain;

    private CookieConfig cookieConfig;
    private CookieUtils cookieUtils;
    private JwtAuthenticationFilter filter;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        cookieConfig = new CookieConfig();
        cookieUtils = new CookieUtils(cookieConfig);
        filter = new JwtAuthenticationFilter(tokenService, userDetailsService, cookieUtils, refreshTokenService);

        testUser = new User("Alice", "alice@example.com");
        testUser.setRoles(Constants.USER_ROLE_DEFAULT);
        userDetails = new SecurityUser(testUser);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validAccessToken_setsAuthentication() throws Exception {
        String token = "valid-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/user/profile");
        request.setCookies(new Cookie("accessToken", token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenService.extractUsername(token)).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);
        when(tokenService.isAccessTokenValid(token, userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("alice@example.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_expiredAccessToken_autoRefreshesViaService() throws Exception {
        String expiredToken = "expired-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/user/profile");
        request.setCookies(new Cookie("accessToken", expiredToken), new Cookie("refreshToken", "valid-refresh"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenService.extractUsername(expiredToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        Authentication refreshedAuth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(refreshTokenService.refreshTokens(request, response)).thenReturn(Optional.of(refreshedAuth));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("alice@example.com");
        verify(refreshTokenService).refreshTokens(request, response);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_expiredAccessToken_refreshFails_clearsContext() throws Exception {
        String expiredToken = "expired-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/user/profile");
        request.setCookies(new Cookie("accessToken", expiredToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenService.extractUsername(expiredToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));
        when(refreshTokenService.refreshTokens(request, response)).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(refreshTokenService).handleInvalidToken(response);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingAccessToken_doesNotAttemptRefresh() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/user/profile");
        request.setCookies(new Cookie("refreshToken", "valid-refresh"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(refreshTokenService, never()).refreshTokens(any(), any());
        verify(filterChain).doFilter(request, response);
    }
}
