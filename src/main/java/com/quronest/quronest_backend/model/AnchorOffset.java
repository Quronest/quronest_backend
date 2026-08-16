package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnchorOffset {
    @JsonProperty("start")
    private Integer start;

    @JsonProperty("end")
    private Integer end;
}
