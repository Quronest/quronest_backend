package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyPlanStatus;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import com.quronest.quronest_backend.model.table.DailyPlan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPlanDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("plan_date")
    private LocalDate planDate;

    @JsonProperty("day_number")
    private Integer dayNumber;

    @JsonProperty("group")
    private UserGroup group;

    @JsonProperty("phase")
    private UserPhase phase;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_base_plan")
    private Boolean isBasePlan;

    @JsonProperty("is_adjusted")
    private Boolean isAdjusted;

    @JsonProperty("adjustment_reason")
    private String adjustmentReason;

    @JsonProperty("status")
    private DailyPlanStatus status;

    @JsonProperty("expected_total_time")
    private Integer expectedTotalTime;

    @JsonProperty("actual_time_spent")
    private Integer actualTimeSpent;

    @JsonProperty("progress_percent")
    private Integer progressPercent;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("creation_timestamp")
    private LocalDateTime creationTimestamp;

    @JsonProperty("update_timestamp")
    private LocalDateTime updateTimestamp;

    @JsonProperty("tasks")
    private List<DailyTaskSummaryDto> tasks;

    public DailyPlanDto(DailyPlan plan, List<DailyTaskSummaryDto> tasks) {
        this.id = plan.getId();
        if (plan.getUser() != null) {
            this.userId = plan.getUser().getId();
        }
        this.planDate = plan.getPlanDate();
        this.dayNumber = plan.getDayNumber();
        this.group = plan.getGroup();
        this.phase = plan.getPhase();
        this.title = plan.getTitle();
        this.description = plan.getDescription();
        this.isBasePlan = plan.getIsBasePlan();
        this.isAdjusted = plan.getIsAdjusted();
        this.adjustmentReason = plan.getAdjustmentReason();
        this.status = plan.getStatus();
        this.expectedTotalTime = plan.getExpectedTotalTime();
        this.actualTimeSpent = plan.getActualTimeSpent();
        this.progressPercent = plan.getProgressPercent();
        this.version = plan.getVersion();
        this.creationTimestamp = plan.getCreationTimestamp();
        this.updateTimestamp = plan.getUpdateTimestamp();

        this.tasks = tasks;
    }
}
