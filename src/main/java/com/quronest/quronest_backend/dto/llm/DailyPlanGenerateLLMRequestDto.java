package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPlanGenerateLLMRequestDto {
    @JsonProperty("user_context")
    private LLMUserContextDto userContext;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("limit")
    private Integer limit;

    public DailyPlanGenerateLLMRequestDto(LLMUserContextDto userContext) {
        this.userContext = userContext;
    }
}
