package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class LLMJobResultEventDto {

    @JsonProperty("event_id")
    @JsonAlias({"uuid", "id"})
    private UUID eventId;

    @JsonProperty("job_result")
    private LLMJobResultDto jobResult;

    @JsonProperty("published_at")
    private String publishedAt;
}
