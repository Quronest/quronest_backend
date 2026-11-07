package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
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
public class QuizPerformanceData {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("total_points")
    private Integer totalPoints;

    @JsonProperty("max_points")
    private Integer maxPoints;

    @JsonProperty("submissions")
    private List<QuizSubmitResponseDto> submissions = new ArrayList<>();
}
