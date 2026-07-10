package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.service.JobHandlerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/jobs")
public class JobController {
    private final JobHandlerService jobHandlerService;

    public JobController(JobHandlerService jobHandlerService) {
        this.jobHandlerService = jobHandlerService;
    }

    @GetMapping("/:jobId")
    public JobStatusDto getJobStatusById(@PathVariable UUID jobId) {
        return jobHandlerService.getJobStatus(jobId);
    }
}
