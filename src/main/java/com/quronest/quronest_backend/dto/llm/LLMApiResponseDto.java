package com.quronest.quronest_backend.dto.llm;

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
public class LLMApiResponseDto<T> {
    @JsonProperty("statuscode")
    private Integer statusCode;

    @JsonProperty("data")
    private T data;

    @JsonProperty("message")
    private String message;

    @JsonProperty("success")
    private boolean success;
}
