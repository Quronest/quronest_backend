package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyTaskPlanLLMResponseDto {
    @JsonProperty("task_number")
    private Integer taskNumber;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private DailyTaskType type;

    @JsonProperty("expected_total_minutes")
    private Integer expectedTotalMinutes;
}
