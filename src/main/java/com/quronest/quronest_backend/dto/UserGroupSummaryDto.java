package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupSummaryDto {
    @JsonProperty("group")
    private String group;

    @JsonProperty("phase")
    private String phase;

    @JsonProperty("summary")
    private String summary;
}
