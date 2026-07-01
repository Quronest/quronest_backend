package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPlanGenerateLLMRequestDto {
    @JsonProperty("user_context")
    private LLMUserContextDto userContext;
}
