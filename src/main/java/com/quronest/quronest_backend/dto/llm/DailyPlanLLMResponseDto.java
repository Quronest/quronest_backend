package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPlanLLMResponseDto {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("day_number")
    private Integer dayNumber;

    @JsonProperty("llm_context")
    private String llmContext;

    @JsonProperty("tasks")
    private List<DailyTaskPlanLLMResponseDto> tasks;
}
