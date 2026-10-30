package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.Constants;
import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.dto.TokenPair;
import com.quronest.quronest_backend.model.table.RefreshToken;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.RefreshTokenRepository;
import com.quronest.quronest_backend.security.CookieUtils;
import com.quronest.quronest_backend.security.TokenService;
import com.quronest.quronest_backend.utils.CryptoUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenService tokenService;

    private CookieConfig cookieConfig;
    private CookieUtils cookieUtils;
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        cookieConfig = new CookieConfig();
        cookieUtils = new CookieUtils(cookieConfig);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, tokenService, cookieUtils, cookieConfig);

        testUser = new User("Alice", "alice@example.com");
        testUser.setRoles(Constants.USER_ROLE_DEFAULT);
    }

    @Test
    void createRefreshToken_storesHashedTokenAndReturnsEntity() {
        String rawToken = "raw-refresh-token-xyz";
        String expectedHash = CryptoUtils.hash(rawToken);
        LocalDateTime expectedExpiry = LocalDateTime.now().plusDays(7);

        when(tokenService.calculateRefreshTokenExpiryDate()).thenReturn(expectedExpiry);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken saved = refreshTokenService.createRefreshToken(testUser, rawToken);

        assertThat(saved).isNotNull();
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getToken()).isEqualTo(expectedHash);
        assertThat(saved.getExpiryDate()).isEqualTo(expectedExpiry);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo(expectedHash);
    }

    @Test
    void refreshTokens_cookieMissing_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Optional<Authentication> result = refreshTokenService.refreshTokens(request, response);

        assertThat(result).isEmpty();
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void refreshTokens_hashNotFoundInDb_returnsEmptyAndClearsCookies() {
        String rawToken = "unknown-token";
        String hashed = CryptoUtils.hash(rawToken);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", rawToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(refreshTokenRepository.findByToken(hashed)).thenReturn(Optional.empty());

        Optional<Authentication> result = refreshTokenService.refreshTokens(request, response);

        assertThat(result).isEmpty();
        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(c -> assertThat(c).contains("accessToken=").contains("Max-Age=0"))
                .anySatisfy(c -> assertThat(c).contains("refreshToken=").contains("Max-Age=0"));
    }

    @Test
    void refreshTokens_tokenExpiredInDb_deletesTokenAndReturnsEmpty() {
        String rawToken = "expired-token";
        String hashed = CryptoUtils.hash(rawToken);

        RefreshToken dbToken = new RefreshToken(testUser, hashed, LocalDateTime.now().minusMinutes(5));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", rawToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(refreshTokenRepository.findByToken(hashed)).thenReturn(Optional.of(dbToken));

        Optional<Authentication> result = refreshTokenService.refreshTokens(request, response);

        assertThat(result).isEmpty();
        verify(refreshTokenRepository).delete(dbToken);
    }

    @Test
    void refreshTokens_userBlacklisted_deletesTokenAndReturnsEmpty() {
        String rawToken = "valid-token-blacklisted-user";
        String hashed = CryptoUtils.hash(rawToken);

        testUser.setBlacklisted(true);
        RefreshToken dbToken = new RefreshToken(testUser, hashed, LocalDateTime.now().plusDays(1));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", rawToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(refreshTokenRepository.findByToken(hashed)).thenReturn(Optional.of(dbToken));

        Optional<Authentication> result = refreshTokenService.refreshTokens(request, response);

        assertThat(result).isEmpty();
        verify(refreshTokenRepository).delete(dbToken);
    }

    @Test
    void refreshTokens_success_rotatesTokenUpdatesRowAndSetsCookies() {
        String oldRawToken = "valid-current-refresh-token";
        String oldHashedToken = CryptoUtils.hash(oldRawToken);
        LocalDateTime oldExpiry = LocalDateTime.now().plusDays(1);

        RefreshToken dbToken = new RefreshToken(testUser, oldHashedToken, oldExpiry);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", oldRawToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(refreshTokenRepository.findByToken(oldHashedToken)).thenReturn(Optional.of(dbToken));

        String newAccessToken = "new-access-token-123";
        String newRefreshToken = "new-refresh-token-456";
        TokenPair newPair = new TokenPair(newAccessToken, newRefreshToken);
        when(tokenService.generateTokenPair(any(UserDetails.class))).thenReturn(newPair);

        LocalDateTime newExpiry = LocalDateTime.now().plusDays(7);
        when(tokenService.calculateRefreshTokenExpiryDate()).thenReturn(newExpiry);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Authentication> result = refreshTokenService.refreshTokens(request, response);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("alice@example.com");

        // Verify the existing DB row was updated with new token hash and expiry
        verify(refreshTokenRepository).save(dbToken);
        assertThat(dbToken.getToken()).isEqualTo(CryptoUtils.hash(newRefreshToken));
        assertThat(dbToken.getExpiryDate()).isEqualTo(newExpiry);

        // Verify response cookies contain new tokens
        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(c -> assertThat(c).contains("accessToken=" + newAccessToken))
                .anySatisfy(c -> assertThat(c).contains("refreshToken=" + newRefreshToken));
    }

    @Test
    void revokeRefreshToken_deletesRowAndClearsCookies() {
        String rawToken = "logout-token";
        String hashed = CryptoUtils.hash(rawToken);
        RefreshToken dbToken = new RefreshToken(testUser, hashed, LocalDateTime.now().plusDays(1));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", rawToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(refreshTokenRepository.findByToken(hashed)).thenReturn(Optional.of(dbToken));

        refreshTokenService.revokeRefreshToken(request, response);

        verify(refreshTokenRepository).delete(dbToken);
        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(c -> assertThat(c).contains("accessToken=").contains("Max-Age=0"))
                .anySatisfy(c -> assertThat(c).contains("refreshToken=").contains("Max-Age=0"));
    }
}
