package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.DailyPlanSummaryDto;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanGenerateLLMRequestDto;
import com.quronest.quronest_backend.dto.llm.LLMTaskGenerateContextDto;
import com.quronest.quronest_backend.dto.llm.UserGroupSummaryGenerateDto;
import com.quronest.quronest_backend.exception.JobHandleException;
import com.quronest.quronest_backend.exception.JobNotFoundException;
import com.quronest.quronest_backend.exception.LLMApiResponseException;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JobHandlerService {
    private final WebSocketService webSocketService;
    private final JobRepository jobRepository;
    private final UserJourneyService userJourneyService;
    private final DailyPlanService dailyPlanService;
    private final DailyTaskService dailyTaskService;
    private final UserService userService;

    public JobHandlerService(WebSocketService webSocketService,
                             JobRepository jobRepository,
                             UserJourneyService userJourneyService,
                             DailyPlanService dailyPlanService, DailyTaskService dailyTaskService,
                             UserService userService) {
        this.webSocketService = webSocketService;
        this.jobRepository = jobRepository;
        this.userJourneyService = userJourneyService;
        this.dailyPlanService = dailyPlanService;
        this.dailyTaskService = dailyTaskService;
        this.userService = userService;
    }

    public JobStatusDto getJobStatus(UUID jobId) {
        User user = userService.getAuthenticatedUser();
        Job job = jobRepository.findByIdAndUser(jobId, user);
        if (job == null) {
            throw new JobNotFoundException();
        }

        return new JobStatusDto(job);
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
                case LLM_GENERATE_DAILY_TASK_READING -> completeReadingTaskGenerateJob(job);
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

        job.setResultMetadataAs(userGroupSummaryDto);
        jobRepository.save(job);

        // publish ws event
        webSocketService.sendJobCompletedUpdate(job, userGroupSummaryDto);
    }

    private void completeNextDailyPlanGenerateJob(Job job) throws JobHandleException, LLMApiResponseException {
        User user = job.getUser();
        DailyPlanGenerateLLMRequestDto llmRequestDto = job.getMetadataAs(DailyPlanGenerateLLMRequestDto.class);
        if (llmRequestDto == null) {
            throw new JobHandleException("LLM request dto not found.");
        }

        List<DailyPlan> dailyPlans = dailyPlanService.completeNextDailyPlanGeneration(user,
                                                                                      llmRequestDto);

        List<DailyPlanSummaryDto> dailyPlanSummaryDtos = dailyPlans.stream().map(DailyPlanSummaryDto::new).toList();

        job.setResultMetadataAs(dailyPlanSummaryDtos);
        jobRepository.save(job);

        webSocketService.sendJobCompletedUpdate(job, dailyPlanSummaryDtos);
    }

    private void completeReadingTaskGenerateJob(Job job) {
        User user = job.getUser();
        LLMTaskGenerateContextDto taskGenerateContextDto = job.getMetadataAs(LLMTaskGenerateContextDto.class);
        if (taskGenerateContextDto == null) {
            throw new JobHandleException("Task generate context not found.");
        }

        DailyTask task = dailyTaskService.completeReadingTaskGeneration(user,
                                                                        taskGenerateContextDto);

        job.setResultMetadata(task.getContent());
        jobRepository.save(job);

        webSocketService.sendJobCompletedUpdate(job, new DailyTaskDto(task));
    }
}
