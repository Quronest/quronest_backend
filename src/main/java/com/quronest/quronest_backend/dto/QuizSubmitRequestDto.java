package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizSubmitRequestDto {
    @NotNull
    @JsonProperty("answers")
    private Map<Integer, Integer> answers = new HashMap<>();

    @JsonProperty("total_time_spent")
    private Integer totalTimeSpent = 0;
}
