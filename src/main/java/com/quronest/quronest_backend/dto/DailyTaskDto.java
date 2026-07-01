package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.model.enums.DailyTaskLevel;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.Domain;
import com.quronest.quronest_backend.model.table.DailyTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyTaskDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("plan_id")
    private UUID planId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("order")
    private Integer order;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("task_type")
    private DailyTaskType taskType;

    @JsonProperty("domain")
    private Domain domain;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("level")
    private DailyTaskLevel level;

    @JsonProperty("content")
    private JsonNode content;

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

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("creation_timestamp")
    private LocalDateTime creationTimestamp;

    @JsonProperty("update_timestamp")
    private LocalDateTime updateTimestamp;

    public DailyTaskDto(DailyTask task) {
        if (task != null) {
            this.id = task.getId();
            if (task.getPlan() != null) {
                this.planId = task.getPlan().getId();
            }
            if (task.getUser() != null) {
                this.userId = task.getUser().getId();
            }
            this.order = task.getOrder();
            this.title = task.getTitle();
            this.description = task.getDescription();
            this.taskType = task.getTaskType();
            this.domain = task.getDomain();
            this.tags = task.getTags();
            this.level = task.getLevel();
            this.content = task.getContent();
            this.status = task.getStatus();
            this.expectedTotalTime = task.getExpectedTotalTime();
            this.actualTimeSpent = task.getActualTimeSpent();
            this.progressPercent = task.getProgressPercent();
            this.isOptional = task.getIsOptional();
            this.version = task.getVersion();
            this.creationTimestamp = task.getCreationTimestamp();
            this.updateTimestamp = task.getUpdateTimestamp();
        }
    }
}
