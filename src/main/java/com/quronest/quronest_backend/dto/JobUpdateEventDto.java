package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quronest.quronest_backend.model.enums.JobStatus;
import jakarta.persistence.Transient;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobUpdateEventDto {
    @JsonProperty("job_id")
    private UUID jobId;

    @JsonProperty("job_status")
    private JobStatus jobStatus;

    @JsonProperty("metadata")
    private JsonNode metadata;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public <T> JobUpdateEventDto(UUID jobId, JobStatus jobStatus, T metadata) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.metadata = objectMapper.valueToTree(metadata);
    }
}
