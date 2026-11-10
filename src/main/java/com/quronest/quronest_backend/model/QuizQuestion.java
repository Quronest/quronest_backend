package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.QuizQuestionType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class QuizQuestion {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("type")
    private QuizQuestionType type = QuizQuestionType.MCQ;

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

    @JsonProperty("max_points")
    private Integer maxPoints;
}
