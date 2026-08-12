package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.table.DailyPlan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LLMDailyPlanContextDto {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("llm_context")
    private String llmContext;

    public LLMDailyPlanContextDto(DailyPlan dailyPlan) {
        this.title = dailyPlan.getTitle();
        this.description = dailyPlan.getDescription();
        this.llmContext = dailyPlan.getLlmContext();
    }
}
