package com.quronest.quronest_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.cookie")
@Getter
@Setter
public class CookieConfig {
    private boolean secure;
    private int accessTokenMaxAge = 15 * 60;        // 15 minutes
    private int refreshTokenMaxAge = 7 * 24 * 60;   // 7 days
}
