package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.DailyPlanSummaryDto;
import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanGenerateLLMRequestDto;
import com.quronest.quronest_backend.dto.llm.UserGroupSummaryGenerateDto;
import com.quronest.quronest_backend.exception.JobHandleException;
import com.quronest.quronest_backend.exception.LLMApiResponseException;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import com.quronest.quronest_backend.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobHandlerService {
    private final WebSocketService webSocketService;
    private final JobRepository jobRepository;
    private final UserJourneyService userJourneyService;
    private final DailyPlanService dailyPlanService;

    public JobHandlerService(WebSocketService webSocketService,
                             JobRepository jobRepository,
                             UserJourneyService userJourneyService,
                             DailyPlanService dailyPlanService) {
        this.webSocketService = webSocketService;
        this.jobRepository = jobRepository;
        this.userJourneyService = userJourneyService;
        this.dailyPlanService = dailyPlanService;
    }

    public void handleJob(Job job) {
        // lock job
        job.setStatus(JobStatus.LOCKED);
        jobRepository.save(job);

        try {
            // job type cases
            switch (job.getType()) {
                case LLM_GENERATE_USER_SUMMARY -> completeUserSummaryGenerateJob(job);
                case LLM_GENERATE_DAILY_PLAN -> completeNextDailyPlanGenerateJob(job);
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
        UserGroupSummaryDto userGroupSummaryDto = userJourneyService.completeUserJourneyGeneration(user,
                                                                                                   groupSummaryGenerateDto);

        // publish ws event
        webSocketService.sendJobCompletedUpdate(job, userGroupSummaryDto);
    }

    private void completeNextDailyPlanGenerateJob(Job job) throws JobHandleException, LLMApiResponseException {
        User user = job.getUser();
        DailyPlanGenerateLLMRequestDto llmRequestDto = job.getMetadataAs(DailyPlanGenerateLLMRequestDto.class);
        if (llmRequestDto == null) {
            throw new JobHandleException("LLM request dto not found.");
        }

        List<DailyPlanSummaryDto> dailyPlanSummaryDtos = dailyPlanService.completeNextDailyPlanGeneration(user,
                                                                                                          llmRequestDto);

        webSocketService.sendJobCompletedUpdate(job, dailyPlanSummaryDtos);
    }
}
