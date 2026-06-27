package com.quronest.quronest_backend.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "security.custom")
@Getter
@Setter
public class SecurityParameters {

    private String corsdomain = null;
    private String frontendbaseurl = "https://quronest.com";
    private String llmservicebaseurl = "https://quronest.com/llm/api/v1";
    private String loginpage = null;
    private String homepage = null;


    public String getLoginpage() {
        if (loginpage != null) {
            return loginpage;
        }
        return frontendbaseurl + "/login";
    }

    public String getHomepage() {
        if (homepage != null) {
            return homepage;
        }
        return frontendbaseurl + "/home";
    }
}
