package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.table.DailyTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizSubmitResponseDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("task_log_id")
    private UUID taskLogId;

    @JsonProperty("task_id")
    private UUID taskId;

    @JsonProperty("status")
    private DailyTaskStatus status;

    @JsonProperty("total_questions")
    private Integer totalQuestions = 0;

    @JsonProperty("total_correct")
    private Integer totalCorrect = 0;

    @JsonProperty("total_points")
    private Integer totalPoints = 0;

    @JsonProperty("max_points")
    private Integer maxPoints = 0;

    @JsonProperty("score_percentage")
    private Double scorePercentage = 0.0;

    @JsonProperty("passed")
    private Boolean passed = false;

    @JsonProperty("pass_threshold")
    private Double passThreshold = 30.0;

    @JsonProperty("total_time_spent")
    private Integer totalTimeSpent = 0;

    @JsonProperty("calculated_progress")
    private Integer calculatedProgress = 0;

    @JsonProperty("validation_job_id")
    private UUID validationJobId;

    @JsonProperty("questions")
    private List<QuizQuestionResultDto> questions = new ArrayList<>();

    public QuizSubmitResponseDto(UUID id, UUID taskLogId, DailyTask task, Integer totalQuestions,
                                 Integer totalCorrect, Integer totalPoints, Integer maxPoints,
                                 Double scorePercentage, Boolean passed, Double passThreshold,
                                 Integer totalTimeSpent, Integer calculatedProgress,
                                 List<QuizQuestionResultDto> questions) {
        this.id = id;
        this.taskLogId = taskLogId;
        if (task != null) {
            this.taskId = task.getId();
            this.status = task.getStatus();
        }
        this.totalQuestions = totalQuestions;
        this.totalCorrect = totalCorrect;
        this.totalPoints = totalPoints;
        this.maxPoints = maxPoints;
        this.scorePercentage = scorePercentage;
        this.passed = passed;
        this.passThreshold = passThreshold;
        this.totalTimeSpent = totalTimeSpent;
        this.calculatedProgress = calculatedProgress;
        this.questions = questions;
    }
}
