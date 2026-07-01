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
public class ErrorMessageDto {
    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("source")
    private String source;
}
