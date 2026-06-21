package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SourceUrl {
    @JsonProperty("url")
    private String url;

    @JsonProperty("source")
    private String source;
}
