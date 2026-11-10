package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class QuizSubmitRequestDto {
    @JsonProperty("mcq_answers")
    private Map<Integer, Integer> mcqAnswers = new HashMap<>();

    @JsonProperty("saq_answers")
    private Map<Integer, String> saqAnswers = new HashMap<>();

    @JsonProperty("total_time_spent")
    private Integer totalTimeSpent = 0;

    @JsonSetter("answers")
    public void setLegacyAnswers(Map<Integer, Integer> answers) {
        if (answers != null && this.mcqAnswers.isEmpty()) {
            this.mcqAnswers = answers;
        }
    }

    public QuizSubmitRequestDto(Map<Integer, Integer> mcqAnswers, Map<Integer, String> saqAnswers,
                                Integer totalTimeSpent) {
        if (mcqAnswers != null) {
            this.mcqAnswers = mcqAnswers;
        }
        if (saqAnswers != null) {
            this.saqAnswers = saqAnswers;
        }
        if (totalTimeSpent != null) {
            this.totalTimeSpent = totalTimeSpent;
        }
    }

    public QuizSubmitRequestDto(Map<Integer, Integer> mcqAnswers, Integer totalTimeSpent) {
        if (mcqAnswers != null) {
            this.mcqAnswers = mcqAnswers;
        }
        if (totalTimeSpent != null) {
            this.totalTimeSpent = totalTimeSpent;
        }
    }
}
