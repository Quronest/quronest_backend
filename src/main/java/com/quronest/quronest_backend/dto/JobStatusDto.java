package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.Job;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobStatusDto {
    @JsonProperty("job_id")
    private UUID jobId;

    @JsonProperty("job_status")
    private JobStatus jobStatus;

    public JobStatusDto(Job job) {
        this.jobId = job.getId();
        this.jobStatus = job.getStatus();
    }
}
