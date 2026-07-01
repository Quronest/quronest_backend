package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.LLMServiceUrls;
import com.quronest.quronest_backend.dto.*;
import com.quronest.quronest_backend.dto.llm.DailyPlanGenerateLLMRequestDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.LLMApiResponseDto;
import com.quronest.quronest_backend.dto.llm.UserGroupSummaryGenerateDto;
import com.quronest.quronest_backend.exception.LLMApiResponseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class LLMApiService {
    private static final Log log = LogFactory.getLog(LLMApiService.class);
    private final WebClient webClient;

    public LLMApiService(@Qualifier("llmWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public UserGroupSummaryDto generateUserGroupSummary(UserGroupSummaryGenerateDto groupSummaryGenerateDto)
            throws LLMApiResponseException {
        ParameterizedTypeReference<LLMApiResponseDto<UserGroupSummaryDto>> typeReference =
                new ParameterizedTypeReference<>() {
                };
        LLMApiResponseDto<UserGroupSummaryDto> response = post(LLMServiceUrls.SUMMARY_GENERATE_URI,
                                                               groupSummaryGenerateDto, typeReference)
                .block();

        return getApiResponseData(response);
    }

    public List<DailyPlanLLMResponseDto> generateDailyPlan(DailyPlanGenerateLLMRequestDto llmRequestDto)
            throws LLMApiResponseException {
        ParameterizedTypeReference<LLMApiResponseDto<List<DailyPlanLLMResponseDto>>> typeReference =
                new ParameterizedTypeReference<>() {
                };
        LLMApiResponseDto<List<DailyPlanLLMResponseDto>> response = post(LLMServiceUrls.DAILY_PLAN_GENERATE_URI,
                                                                         llmRequestDto, typeReference).block();

        return getApiResponseData(response);
    }

    private <T> T getApiResponseData(LLMApiResponseDto<T> response) throws LLMApiResponseException {
        if (response == null || !response.isSuccess()) {
            throw new LLMApiResponseException("Failed to get LLM Response.");
        }

        return response.getData();
    }

    private <T> Mono<LLMApiResponseDto<T>> post(String uri, Object body,
                                                ParameterizedTypeReference<LLMApiResponseDto<T>> typeReference) {
        return webClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Empty error body")
                                .flatMap(errorBody -> {
                                    log.error("LLM API error - " + errorBody);
                                    return Mono.error(new RuntimeException("LLM API error."));
                                })
                )
                .bodyToMono(typeReference);
    }
}
