package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class McqQuestion {
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
}
