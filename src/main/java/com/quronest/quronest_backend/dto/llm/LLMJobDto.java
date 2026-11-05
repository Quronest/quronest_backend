package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
public class LLMJobDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("type")
    private JobType type;

    @JsonProperty("metadata")
    private JsonNode metadata;

    @JsonProperty("max_retries")
    private Integer maxRetries;
}
