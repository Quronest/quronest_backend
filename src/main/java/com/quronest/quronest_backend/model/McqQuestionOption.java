package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class McqQuestionOption {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("text")
    private String text;
}
