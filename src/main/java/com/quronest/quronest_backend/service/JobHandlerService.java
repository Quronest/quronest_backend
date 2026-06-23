package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.dto.UserGroupSummaryGenerateDto;
import com.quronest.quronest_backend.exception.JobHandleException;
import com.quronest.quronest_backend.exception.LLMApiResponseException;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.model.table.UserJourney;
import com.quronest.quronest_backend.repository.JobRepository;
import com.quronest.quronest_backend.repository.UserJourneyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobHandlerService {
    private final LLMApiService llmApiService;
    private final UserJourneyRepository userJourneyRepository;
    private final WebSocketService webSocketService;
    private final JobRepository jobRepository;

    public JobHandlerService(LLMApiService llmApiService,
                             UserJourneyRepository userJourneyRepository, WebSocketService webSocketService,
                             JobRepository jobRepository) {
        this.llmApiService = llmApiService;
        this.userJourneyRepository = userJourneyRepository;
        this.webSocketService = webSocketService;
        this.jobRepository = jobRepository;
    }

    public void handleJob(Job job) {
        // lock job
        job.setStatus(JobStatus.LOCKED);
        jobRepository.save(job);

        try {
            switch (job.getType()) {
                case LLM_GENERATE_DAILY_TASK -> completeUserSummaryGenerateJob(job);
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

        UserGroupSummaryDto userGroupSummaryDto = llmApiService.generateUserGroupSummary(groupSummaryGenerateDto);

        UserJourney journey = new UserJourney(user, userGroupSummaryDto.getGroup(), userGroupSummaryDto.getPhase(),
                                              userGroupSummaryDto.getSummary());
        userJourneyRepository.save(journey);

        // update job
        job.markAsCompleted();
        jobRepository.save(job);

        webSocketService.sendJobUpdate(job, userGroupSummaryDto);
    }
}
