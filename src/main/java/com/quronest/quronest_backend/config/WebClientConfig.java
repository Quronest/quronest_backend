package com.quronest.quronest_backend.config;

import com.quronest.quronest_backend.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private final JwtService jwtService;

    public WebClientConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean()
    public WebClient llmWebClient() {
        return WebClient.builder()
                .baseUrl(LLMServiceUrls.API_BASE_URI)
                .defaultHeader("Content-Type", "application/json")
                .filter((request, next) -> {
                    ClientRequest newRequest = ClientRequest.from(request)
                            .headers(headers -> headers.setBearerAuth(jwtService.generateLLMServiceToken()))
                            .build();

                    return next.exchange(newRequest);
                })
                .build();
    }
}
