package com.quronest.quronest_backend.config;

import com.quronest.quronest_backend.security.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private final TokenService tokenService;
    private final SecurityParameters securityParameters;

    public WebClientConfig(TokenService tokenService, SecurityParameters securityParameters) {
        this.tokenService = tokenService;
        this.securityParameters = securityParameters;
    }

    @Bean()
    public WebClient llmWebClient() {
        return WebClient.builder()
                .baseUrl(securityParameters.getLlmservicebaseurl())
                .defaultHeader("Content-Type", "application/json")
                .filter((request, next) -> {
                    ClientRequest newRequest = ClientRequest.from(request)
                            .headers(headers -> headers.setBearerAuth(tokenService.generateLLMServiceToken()))
                            .build();

                    return next.exchange(newRequest);
                })
                .build();
    }
}
