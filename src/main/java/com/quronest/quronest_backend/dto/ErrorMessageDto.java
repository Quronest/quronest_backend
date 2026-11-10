package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessageDto {
    @JsonProperty("error_message")
    @JsonAlias({"message"})
    private String errorMessage;

    @JsonProperty("source")
    @JsonAlias({"error_type"})
    private String source;
}
