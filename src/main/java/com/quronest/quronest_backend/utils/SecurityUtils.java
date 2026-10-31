package com.quronest.quronest_backend.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {

    public static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public record HashedToken(String token, String hash) {
        public String getToken() {
            return token;
        }

        public String getHash() {
            return hash;
        }

        public String getRawToken() {
            return token;
        }

        public String getHashedToken() {
            return hash;
        }
    }

    public static String generateEmailToken() {
        return generateSecureToken(64);
    }

    public static String generateOtp(int otpLength) {
        String chrs = "0123456789";
        return secureRandom.ints(otpLength, 0, chrs.length()).mapToObj(chrs::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    /**
     * Generates a secure token for password reset or invite.
     *
     * @param numBytes number of random bytes to generate (recommended: 16–64)
     * @return a cryptographically secure random string
     */
    public static String generateSecureToken(int numBytes) {
        if (numBytes <= 0) {
            throw new IllegalArgumentException("numBytes must be positive");
        }
        byte[] randomBytes = new byte[numBytes];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * Generates a 16-byte secure token, computes its SHA-256 hash, and returns both.
     *
     * @return a {@link HashedToken} containing the raw token and its SHA-256 hash
     */
    public static HashedToken generateSecureTokenAndHash() {
        return generateSecureTokenAndHash(16);
    }

    /**
     * Generates a secure token of the given byte length, computes its SHA-256 hash, and returns both.
     *
     * @param numBytes number of random bytes to generate
     * @return a {@link HashedToken} containing the raw token and its SHA-256 hash
     */
    public static HashedToken generateSecureTokenAndHash(int numBytes) {
        String token = generateSecureToken(numBytes);
        String hash = CryptoUtils.hash(token);
        return new HashedToken(token, hash);
    }

    /**
     * Generates a 16-bit / 16-byte secure token, computes its SHA-256 hash, and returns both.
     *
     * @return a {@link HashedToken} containing the raw token and its SHA-256 hash
     */
    public static HashedToken generate16BitSecureTokenAndHash() {
        return generateSecureTokenAndHash(16);
    }

    /**
     * Generates a refresh token and its SHA-256 hash.
     *
     * @return a {@link HashedToken} containing the raw refresh token and its hash
     */
    public static HashedToken generateRefreshTokenWithHash() {
        return generateSecureTokenAndHash(16);
    }

    /**
     * Generates a simple 64-byte secure random refresh token string.
     *
     * @return raw refresh token string
     */
    public static String generateRefreshToken() {
        return generateSecureToken(64);
    }

    public static String generateOpaqueToken(int length) {
        String chrs = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String token = secureRandom.ints(length, 0, chrs.length()).mapToObj(chrs::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        return token;
    }
}
