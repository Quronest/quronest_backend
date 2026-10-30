package com.quronest.quronest_backend.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    @Test
    void generateRefreshToken_returnsNonEmptyString() {
        String token = SecurityUtils.generateRefreshToken();
        assertThat(token).isNotBlank();
        // 16 bytes base64-encoded is ~22 characters
        assertThat(token.length()).isGreaterThanOrEqualTo(20);
    }

    @Test
    void generateSecureTokenAndHash_returnsTokenAndMatchingHash() {
        SecurityUtils.HashedToken hashedToken = SecurityUtils.generateSecureTokenAndHash();

        assertThat(hashedToken).isNotNull();
        assertThat(hashedToken.getToken()).isNotBlank();
        assertThat(hashedToken.getHash()).isNotBlank();
        assertThat(hashedToken.getHash()).isEqualTo(CryptoUtils.hash(hashedToken.getToken()));

        // Also test record methods and aliases
        assertThat(hashedToken.token()).isEqualTo(hashedToken.getToken());
        assertThat(hashedToken.hash()).isEqualTo(hashedToken.getHash());
        assertThat(hashedToken.getRawToken()).isEqualTo(hashedToken.getToken());
        assertThat(hashedToken.getHashedToken()).isEqualTo(hashedToken.getHash());
    }

    @Test
    void generate16BitSecureTokenAndHash_returnsHashedToken() {
        SecurityUtils.HashedToken hashedToken = SecurityUtils.generate16BitSecureTokenAndHash();

        assertThat(hashedToken).isNotNull();
        assertThat(hashedToken.getToken()).isNotBlank();
        assertThat(hashedToken.getHash()).isEqualTo(CryptoUtils.hash(hashedToken.getToken()));
    }

    @Test
    void generateRefreshTokenWithHash_returnsHashedToken() {
        SecurityUtils.HashedToken hashedToken = SecurityUtils.generateRefreshTokenWithHash();

        assertThat(hashedToken).isNotNull();
        assertThat(hashedToken.getToken()).isNotBlank();
        assertThat(hashedToken.getHash()).isEqualTo(CryptoUtils.hash(hashedToken.getToken()));
    }

    @Test
    void generateSecureToken_differentCallsProduceDifferentTokens() {
        String token1 = SecurityUtils.generateRefreshToken();
        String token2 = SecurityUtils.generateRefreshToken();

        assertThat(token1).isNotEqualTo(token2);
    }
}
