package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.table.DailyTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyTaskSummaryDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("order")
    private Integer order;

    @JsonProperty("title")
    private String title;

    @JsonProperty("task_type")
    private DailyTaskType taskType;

    @JsonProperty("status")
    private DailyTaskStatus status;

    @JsonProperty("expected_total_time")
    private Integer expectedTotalTime;

    @JsonProperty("actual_time_spent")
    private Integer actualTimeSpent;

    @JsonProperty("progress_percent")
    private Integer progressPercent;

    @JsonProperty("is_optional")
    private Boolean isOptional;

    public DailyTaskSummaryDto(DailyTask task) {
        if (task != null) {
            this.id = task.getId();
            this.order = task.getOrder();
            this.title = task.getTitle();
            this.taskType = task.getTaskType();
            this.status = task.getStatus();
            this.expectedTotalTime = task.getExpectedTotalTime();
            this.actualTimeSpent = task.getActualTimeSpent();
            this.progressPercent = task.getProgressPercent();
            this.isOptional = task.getIsOptional();
        }
    }
}
