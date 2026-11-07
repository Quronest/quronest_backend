package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.LLMServiceUrls;
import com.quronest.quronest_backend.dto.llm.LLMAssistantChatContextDto;
import com.quronest.quronest_backend.dto.llm.QuizSaqValidationItemDto;
import com.quronest.quronest_backend.dto.llm.QuizSaqValidationResultItemDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class LLMApiService {
    private static final Log log = LogFactory.getLog(LLMApiService.class);
    private final WebClient webClient;

    public LLMApiService(@Qualifier("llmWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<String> getAssistantChatStream(LLMAssistantChatContextDto assistantChatContextDto) {
        return postStream(LLMServiceUrls.ASSISTANT_CHAT_URI, assistantChatContextDto, String.class);
    }

    public List<QuizSaqValidationResultItemDto> verifySaqQuestions(List<QuizSaqValidationItemDto> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return webClient.post()
                .uri(LLMServiceUrls.QUIZ_VERIFY_SAQ_URI)
                .bodyValue(items)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleApiError)
                .bodyToFlux(QuizSaqValidationResultItemDto.class)
                .collectList()
                .block();
    }

    private <T> Flux<T> postStream(String uri, Object body, Class<T> elementClass) {
        return webClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleApiError)
                .bodyToFlux(elementClass);
    }

    private Mono<? extends Throwable> handleApiError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Empty error body")
                .flatMap(errorBody -> {
                    log.error("LLM API error - " + errorBody);
                    return Mono.error(new RuntimeException("LLM API error."));
                });
    }
}
