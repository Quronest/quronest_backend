package com.quronest.quronest_backend.utils;

import java.util.Locale;

public class EmailNormalizer {
    public static String normalize(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1].toLowerCase(Locale.ROOT);

        // Domain-specific rules (e.g., Gmail)
        if (domain.equals("gmail.com") || domain.equals("googlemail.com")) {
            domain = "gmail.com";
            // Remove everything after '+' and remove all dots
            localPart = localPart.split("\\+")[0].replace(".", "");
        }

        return (localPart + "@" + domain).toLowerCase(Locale.ROOT);
    }
}
