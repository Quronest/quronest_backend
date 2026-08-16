package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.AssistantStreamFlag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssistantStreamDataDto {
    @JsonProperty("flag")
    private AssistantStreamFlag flag;

    @JsonProperty("data")
    private String data;
}
