package com.quronest.quronest_backend.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoUtilsTest {

    @Test
    void hash_producesSha256Hex() {
        String input = "sample-token-string";
        String hash = CryptoUtils.hash(input);

        assertThat(hash).isNotNull();
        // SHA-256 produces a 64-character hexadecimal string
        assertThat(hash).hasSize(64);
        assertThat(hash).matches("^[0-9a-f]{64}$");
    }

    @Test
    void hash_deterministic() {
        String input = "another-token-12345";
        String hash1 = CryptoUtils.hash(input);
        String hash2 = CryptoUtils.hash(input);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hash_differentInputsProduceDifferentHashes() {
        String hash1 = CryptoUtils.hash("token-a");
        String hash2 = CryptoUtils.hash("token-b");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void hash_nullInput_returnsNull() {
        assertThat(CryptoUtils.hash(null)).isNull();
    }

    @Test
    void verify_validatesMatch() {
        String input = "secret-token";
        String hash = CryptoUtils.hash(input);

        assertThat(CryptoUtils.verify(input, hash)).isTrue();
        assertThat(CryptoUtils.verify("wrong-token", hash)).isFalse();
        assertThat(CryptoUtils.verify(null, hash)).isFalse();
        assertThat(CryptoUtils.verify(input, null)).isFalse();
    }
}
