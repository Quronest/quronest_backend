package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.dto.UserGroupSummaryGenerateDto;
import com.quronest.quronest_backend.exception.JobHandleException;
import com.quronest.quronest_backend.exception.LLMApiResponseException;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
public class JobHandlerService {
    private final WebSocketService webSocketService;
    private final JobRepository jobRepository;
    private final UserJourneyService userJourneyService;

    public JobHandlerService(WebSocketService webSocketService,
                             JobRepository jobRepository, UserJourneyService userJourneyService) {
        this.webSocketService = webSocketService;
        this.jobRepository = jobRepository;
        this.userJourneyService = userJourneyService;
    }

    public void handleJob(Job job) {
        // lock job
        job.setStatus(JobStatus.LOCKED);
        jobRepository.save(job);

        try {
            switch (job.getType()) {
                case LLM_GENERATE_USER_SUMMARY -> completeUserSummaryGenerateJob(job);
            }
        } catch (JobHandleException | LLMApiResponseException e) {
            // send failed event in first failure
            webSocketService.sendJobUpdate(job, JobStatus.FAILED, null);

            throw e;
        }
    }

    private void completeUserSummaryGenerateJob(Job job) throws JobHandleException, LLMApiResponseException {
        User user = job.getUser();
        UserGroupSummaryGenerateDto groupSummaryGenerateDto = job.getMetadataAs(UserGroupSummaryGenerateDto.class);
        if (groupSummaryGenerateDto == null) {
            throw new JobHandleException("User summary generate dto not found");
        }

        // get generated summary
        UserGroupSummaryDto userGroupSummaryDto = userJourneyService.completeUserJourneyGeneration(user, groupSummaryGenerateDto);

        // update job
        job.markAsCompleted();
        jobRepository.save(job);

        webSocketService.sendJobUpdate(job, userGroupSummaryDto);
    }
}
