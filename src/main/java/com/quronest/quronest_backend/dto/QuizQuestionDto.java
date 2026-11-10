package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.QuizQuestion;
import com.quronest.quronest_backend.model.McqQuestionOption;
import com.quronest.quronest_backend.model.enums.QuizQuestionType;
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
public class QuizQuestionDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("type")
    private QuizQuestionType type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("options")
    private List<McqQuestionOption> options = new ArrayList<>();

    @JsonProperty("max_points")
    private Integer maxPoints;

    public QuizQuestionDto(QuizQuestion question) {
        this.id = question.getId();
        this.type = question.getType();
        this.title = question.getTitle();
        this.options = question.getOptions();
        this.maxPoints = question.getMaxPoints();
    }
}
