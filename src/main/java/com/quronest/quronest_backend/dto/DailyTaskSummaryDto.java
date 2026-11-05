package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyTaskLevel;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.table.DailyTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
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

    @JsonProperty("description")
    private String description;

    @JsonProperty("task_type")
    private DailyTaskType taskType;

    @JsonProperty("status")
    private DailyTaskStatus status;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("level")
    private DailyTaskLevel level;

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
            this.description = task.getDescription();
            this.taskType = task.getTaskType();
            this.status = task.getStatus();
            this.tags = task.getTags();
            this.level = task.getLevel();
            this.expectedTotalTime = task.getExpectedTotalTime();
            this.actualTimeSpent = task.getActualTimeSpent();
            this.progressPercent = task.getProgressPercent();
            this.isOptional = task.getIsOptional();
        }
    }
}
