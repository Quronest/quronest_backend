package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.JwtConfig;
import com.quronest.quronest_backend.dto.TokenPair;
import com.quronest.quronest_backend.model.table.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private TokenService tokenService;
    private JwtConfig jwtConfig;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        // 256-bit Base64 key for accessToken
        jwtConfig.setAccessTokenSecret("c2tkamhrZHNhamhrZGZqZ2hkZmtqYm5qa2JuamZiZGdqaGdrZmpuYmpmYmhmZGpnaGprZ2g=");
        jwtConfig.setAccessTokenExpiry(60000); // 1 minute
        jwtConfig.setRefreshTokenExpiry(120000); // 2 minutes

        tokenService = new TokenService(jwtConfig);

        User user = new User("Alice", "alice@example.com");
        userDetails = new SecurityUser(user);
    }

    @Test
    void generateTokenPair_returnsBothTokens() {
        TokenPair tokenPair = tokenService.generateTokenPair(userDetails);

        assertThat(tokenPair).isNotNull();
        assertThat(tokenPair.getAccessToken()).isNotBlank();
        assertThat(tokenPair.getRefreshToken()).isNotBlank();
        assertThat(tokenPair.getAccessToken()).isNotEqualTo(tokenPair.getRefreshToken());
    }

    @Test
    void isAccessTokenValid_validatesCorrectUser() {
        String accessToken = tokenService.generateAccessToken(userDetails);

        assertThat(tokenService.isAccessTokenValid(accessToken, userDetails)).isTrue();
        assertThat(tokenService.extractUsername(accessToken)).isEqualTo("alice@example.com");

        User otherUser = new User("Bob", "bob@example.com");
        UserDetails otherDetails = new SecurityUser(otherUser);
        assertThat(tokenService.isAccessTokenValid(accessToken, otherDetails)).isFalse();
    }

    @Test
    void calculateRefreshTokenExpiryDate_returnsFutureDateTime() {
        LocalDateTime expiry = tokenService.calculateRefreshTokenExpiryDate();
        assertThat(expiry).isAfter(LocalDateTime.now().plusSeconds(60));
    }
}
