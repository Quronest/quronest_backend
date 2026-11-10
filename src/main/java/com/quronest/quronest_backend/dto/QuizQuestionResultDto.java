package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.QuizQuestion;
import com.quronest.quronest_backend.model.McqQuestionOption;
import com.quronest.quronest_backend.model.enums.QuizQuestionType;
import com.quronest.quronest_backend.model.enums.EvaluationClass;
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

    @JsonProperty("type")
    private QuizQuestionType type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("options")
    private List<McqQuestionOption> options = new ArrayList<>();

    @JsonProperty("solution_option")
    private McqQuestionOption solutionOption;

    @JsonProperty("solution_text")
    private String solutionText;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("user_answer_option_id")
    private Integer userAnswerOptionId;

    @JsonProperty("user_answer_text")
    private String userAnswerText;

    @JsonProperty("answer_class")
    private EvaluationClass answerClass;

    @JsonProperty("points")
    private Integer points;

    @JsonProperty("max_points")
    private Integer maxPoints;

    @JsonProperty("is_correct")
    private Boolean isCorrect;

    public QuizQuestionResultDto(QuizQuestion question) {
        this.id = question.getId();
        this.type = question.getType();
        this.title = question.getTitle();
        this.options = question.getOptions();
        this.solutionOption = question.getSolutionOption();
        this.solutionText = question.getSolutionText();
        this.explanation = question.getExplanation();
        this.maxPoints = question.getMaxPoints();
    }

    public QuizQuestionResultDto(QuizQuestion question, Integer userAnswerOptionId,
                                 EvaluationClass answerClass, Integer points, Boolean isCorrect) {
        this(question);
        this.userAnswerOptionId = userAnswerOptionId;
        this.answerClass = answerClass;
        this.points = points;
        this.isCorrect = isCorrect;
    }

    public QuizQuestionResultDto(QuizQuestion question, String userAnswerText,
                                 EvaluationClass answerClass, Integer points, Boolean isCorrect) {
        this(question);
        this.userAnswerText = userAnswerText;
        this.answerClass = answerClass;
        this.points = points;
        this.isCorrect = isCorrect;
    }

    public QuizQuestionResultDto(QuizQuestion question, Integer userAnswerOptionId, String userAnswerText,
                                 EvaluationClass answerClass, Integer points, Boolean isCorrect) {
        this(question);
        this.userAnswerOptionId = userAnswerOptionId;
        this.userAnswerText = userAnswerText;
        this.answerClass = answerClass;
        this.points = points;
        this.isCorrect = isCorrect;
    }
}
