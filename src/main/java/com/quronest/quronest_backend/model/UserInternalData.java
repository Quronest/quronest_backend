package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInternalData {
    @JsonProperty("group")
    private String group;

    @JsonProperty("summary")
    private String summary;
}
