package com.quronest.quronest_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LLMApiService {
    private final WebClient webClient;

    public LLMApiService(WebClient webClient) {
        this.webClient = webClient;
    }
}
