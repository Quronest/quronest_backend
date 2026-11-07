package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.EvaluationClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizSaqValidationResultItemDto {
    @JsonProperty("question_id")
    private Integer questionId;

    @JsonProperty("classified_class")
    private EvaluationClass classifiedClass;
}
