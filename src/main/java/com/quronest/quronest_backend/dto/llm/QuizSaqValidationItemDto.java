package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizSaqValidationItemDto {
    @JsonProperty("question_id")
    private Integer questionId;

    @JsonProperty("question_text")
    private String questionText;

    @JsonProperty("solution_text")
    private String solutionText;

    @JsonProperty("answer_text")
    private String answerText;

    public QuizSaqValidationItemDto(QuizQuestion question, String answerText) {
        this.questionId = question.getId();
        this.questionText = question.getTitle();
        this.solutionText = question.getSolutionText();
        this.answerText = answerText;
    }
}
