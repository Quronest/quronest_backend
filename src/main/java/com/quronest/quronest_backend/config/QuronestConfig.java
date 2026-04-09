package com.quronest.quronest_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "quronest")
@Getter
@Setter
public class QuronestConfig {
    private String google_client_id = "";
    private String google_client_secret = "";

    private String github_client_id = "";
    private String github_client_secret = "";
    private String github_userinfo_uri = "https://api.github.com/user";
    private String github_useremail_uri = "https://api.github.com/user/emails";
    private String github_scope = "read:user,user:email";
}
