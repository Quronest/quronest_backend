package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyPlanStatus;
import com.quronest.quronest_backend.model.table.DailyPlan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPlanDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("plan_date")
    private LocalDate planDate;

    @JsonProperty("day_number")
    private Integer dayNumber;

    @JsonProperty("status")
    private DailyPlanStatus status;

    @JsonProperty("progress_percent")
    private Integer progressPercent = 0;

    public DailyPlanDto(DailyPlan plan) {
        this.id = plan.getId();
        this.title = plan.getTitle();
        this.description = plan.getDescription();
        this.planDate = plan.getPlanDate();
        this.dayNumber = plan.getDayNumber();
        this.status = plan.getStatus();
        this.progressPercent = plan.getProgressPercent();
    }
}
