package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LLMJobEventDto {

    @JsonProperty("event_id")
    @JsonAlias({"uuid", "id"})
    private UUID eventId;

    @JsonProperty("job")
    private LLMJobDto job;

    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    @JsonProperty("retries")
    private Integer retries = 0;

    public LLMJobEventDto(UUID eventId, LLMJobDto job, LocalDateTime publishedAt) {
        this.eventId = eventId;
        this.job = job;
        this.publishedAt = publishedAt;
        this.retries = 0;
    }
}
