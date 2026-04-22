package com.quronest.quronest_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class JwtConfig {
    private String accessTokenSecret;
    private long accessTokenExpiry;

    private String refreshTokenSecret;
    private long refreshTokenExpiry;

    private String privateKeyPath;
    private String publicKeyPath;
    private long serviceKyExpiration = 54000000;
}
