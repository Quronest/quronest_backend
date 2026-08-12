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
    @JsonProperty("task_id")
    private UUID taskId;

    @JsonProperty("status")
    private DailyTaskStatus status;

    @JsonProperty("total_questions")
    private Integer totalQuestions = 0;

    @JsonProperty("total_correct")
    private Integer totalCorrect = 0;

    @JsonProperty("score_percentage")
    private Double scorePercentage = 0.0;

    @JsonProperty("passed")
    private Boolean passed = false;

    @JsonProperty("pass_threshold")
    private Double passThreshold = 30.0;

    @JsonProperty("total_time_spent")
    private Integer totalTimeSpent = 0;

    @JsonProperty("questions")
    private List<QuizQuestionResultDto> questions = new ArrayList<>();

    public QuizSubmitResponseDto(DailyTask task, Integer totalQuestions, Integer totalCorrect,
                                 Double scorePercentage, Boolean passed, Double passThreshold,
                                 Integer totalTimeSpent, List<QuizQuestionResultDto> questions) {
        if (task != null) {
            this.taskId = task.getId();
            this.status = task.getStatus();
        }
        this.totalQuestions = totalQuestions;
        this.totalCorrect = totalCorrect;
        this.scorePercentage = scorePercentage;
        this.passed = passed;
        this.passThreshold = passThreshold;
        this.totalTimeSpent = totalTimeSpent;
        this.questions = questions;
    }
}
