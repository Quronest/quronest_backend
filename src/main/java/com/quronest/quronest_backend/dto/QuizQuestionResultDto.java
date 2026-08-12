package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.McqQuestion;
import com.quronest.quronest_backend.model.McqQuestionOption;
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
public class QuizQuestionResultDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("options")
    private List<McqQuestionOption> options = new ArrayList<>();

    @JsonProperty("solution")
    private McqQuestionOption solution;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("user_answer_option_id")
    private Integer userAnswerOptionId;

    @JsonProperty("is_correct")
    private Boolean isCorrect;

    public QuizQuestionResultDto(McqQuestion question, Integer userAnswerOptionId, Boolean isCorrect) {
        if (question != null) {
            this.id = question.getId();
            this.title = question.getTitle();
            this.options = question.getOptions();
            this.solution = question.getSolution();
            this.explanation = question.getExplanation();
        }
        this.userAnswerOptionId = userAnswerOptionId;
        this.isCorrect = isCorrect;
    }
}
