package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyTaskLevel;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.Domain;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LLMTaskContextDto {
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

    @JsonProperty("subdomains")
    private List<String> subdomains = new ArrayList<>();

    @JsonProperty("tags")
    private List<String> tags = new ArrayList<>();

    @JsonProperty("level")
    private DailyTaskLevel level;

    @JsonProperty("content")
    private Object content;

    @JsonProperty("status")
    private DailyTaskStatus status;

    @JsonProperty("expected_total_minutes")
    private Integer expectedTotalMinutes;

    @JsonProperty("actual_time_spent")
    private Integer actualTimeSpent;

    @JsonProperty("progress_percent")
    private Integer progressPercent;

    @JsonProperty("is_optional")
    private Boolean isOptional;

    @JsonProperty("llm_context")
    private String llmContext;

    public LLMTaskContextDto(DailyTask task) {
        if (task != null) {
            this.order = task.getOrder();
            this.title = task.getTitle();
            this.description = task.getDescription();
            this.taskType = task.getTaskType();
            this.domain = task.getDomain();
            this.subdomains = task.getSubdomains();
            this.tags = task.getTags();
            this.level = task.getLevel();
            this.content = JsonUtils.fromJsonNode(task.getContent(), Object.class);
            this.status = task.getStatus();
            this.expectedTotalMinutes = task.getExpectedTotalTime();
            this.actualTimeSpent = task.getActualTimeSpent();
            this.progressPercent = task.getProgressPercent();
            this.isOptional = task.getIsOptional();
            this.llmContext = task.getLlmContext();
        }
    }
}
