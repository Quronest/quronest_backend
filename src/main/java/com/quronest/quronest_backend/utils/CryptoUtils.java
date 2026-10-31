package com.quronest.quronest_backend.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class CryptoUtils {

    private CryptoUtils() {
    }

    /**
     * Computes a SHA-256 hash of the given input string.
     *
     * @param input the input string to hash
     * @return the hex-encoded SHA-256 hash string, or null if input is null
     */
    public static String hash(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Alias for {@link #hash(String)}.
     *
     * @param input the input string to hash
     * @return the hex-encoded SHA-256 hash string, or null if input is null
     */
    public static String sha256(String input) {
        return hash(input);
    }

    /**
     * Verifies whether the raw input string produces the expected hash using a constant-time comparison.
     *
     * @param raw    the raw input string
     * @param hashed the expected hash string
     * @return true if matches, false otherwise
     */
    public static boolean verify(String raw, String hashed) {
        if (raw == null || hashed == null) {
            return false;
        }
        String computed = hash(raw);
        return MessageDigest.isEqual(computed.getBytes(StandardCharsets.UTF_8),
                                     hashed.getBytes(StandardCharsets.UTF_8));
    }
}
