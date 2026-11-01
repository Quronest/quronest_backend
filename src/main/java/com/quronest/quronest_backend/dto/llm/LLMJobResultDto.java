package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.dto.ErrorMessageDto;
import com.quronest.quronest_backend.model.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LLMJobResultDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("type")
    private JobType type;

    @JsonProperty("status")
    private String status;

    @JsonProperty("retries")
    private Integer retries = 0;

    @JsonProperty("attempts")
    private Integer attempts = 1;

    @JsonProperty("payload")
    private JsonNode payload;

    @JsonProperty("error")
    private ErrorMessageDto error;

    public LLMJobResultDto(UUID id, JobType type, String status, JsonNode payload, ErrorMessageDto error) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.payload = payload;
        this.error = error;
        this.retries = 0;
        this.attempts = 1;
    }
}
