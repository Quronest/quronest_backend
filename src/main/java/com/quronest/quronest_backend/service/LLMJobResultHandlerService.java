package com.quronest.quronest_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.dto.DailyPlanSummaryDto;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.ErrorMessageDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.LLMJobResultDto;
import com.quronest.quronest_backend.dto.llm.LLMJobResultEventDto;
import com.quronest.quronest_backend.dto.llm.LLMTaskGenerateContextDto;
import com.quronest.quronest_backend.exception.JobHandleException;
import com.quronest.quronest_backend.exception.JobNotFoundException;
import com.quronest.quronest_backend.model.QuizTaskContent;
import com.quronest.quronest_backend.model.ReadingTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import com.quronest.quronest_backend.utils.JsonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LLMJobResultHandlerService {
    private static final Log log = LogFactory.getLog(LLMJobResultHandlerService.class);

    private final WebSocketService webSocketService;
    private final JobRepository jobRepository;
    private final UserJourneyService userJourneyService;
    private final DailyPlanService dailyPlanService;
    private final DailyTaskService dailyTaskService;
    private final UserService userService;

    public LLMJobResultHandlerService(WebSocketService webSocketService,
                                      JobRepository jobRepository,
                                      UserJourneyService userJourneyService,
                                      DailyPlanService dailyPlanService,
                                      DailyTaskService dailyTaskService,
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

    public void handleJobResult(LLMJobResultEventDto eventDto) {
        if (eventDto == null || eventDto.getJobResult() == null) {
            log.warn("Received empty or null LLMJobResultEventDto");
            return;
        }

        LLMJobResultDto jobResult = eventDto.getJobResult();
        Job job = jobRepository.findById(jobResult.getId()).orElse(null);
        if (job == null) {
            log.warn("Job not found for result ID: " + jobResult.getId());
            return;
        }

        // Update retry count from result
        if (jobResult.getRetries() != null) {
            job.setRetries(jobResult.getRetries());
        }

        if (JobStatus.FAILED.name().equalsIgnoreCase(jobResult.getStatus())) {
            handleFailedJob(job, jobResult.getError());
            return;
        }

        try {
            switch (job.getType()) {
                case LLM_GENERATE_USER_SUMMARY -> completeUserSummaryFromResult(job, jobResult.getPayload());
                case LLM_GENERATE_DAILY_PLAN -> completeDailyPlanFromResult(job, jobResult.getPayload());
                case LLM_GENERATE_DAILY_TASK_READING ->
                        completeTaskFromResult(job, jobResult.getPayload(), DailyTaskType.READING);
                case LLM_GENERATE_DAILY_TASK_QUIZ ->
                        completeTaskFromResult(job, jobResult.getPayload(), DailyTaskType.QUIZ);
            }
        } catch (Exception e) {
            log.error("Failed to complete job processing for " + job.getId(), e);
            handleFailedJob(job, new ErrorMessageDto(e.getMessage(), e.getClass().getSimpleName()));
        }
    }

    private void completeUserSummaryFromResult(Job job, JsonNode resultNode) throws JobHandleException {
        if (resultNode == null || resultNode.isNull()) {
            throw new JobHandleException("Result payload is empty for user summary job.");
        }

        User user = job.getUser();
        UserGroupSummaryDto userGroupSummaryDto = JsonUtils.fromJsonNode(resultNode, UserGroupSummaryDto.class);
        if (userGroupSummaryDto == null) {
            throw new JobHandleException("Failed to parse UserGroupSummaryDto from result.");
        }

        userJourneyService.saveUserJourneyFromSummary(user, userGroupSummaryDto);

        job.setResultMetadata(resultNode);
        job.markAsCompleted();
        jobRepository.save(job);

        webSocketService.sendJobCompletedUpdate(job, userGroupSummaryDto);
    }

    private void completeDailyPlanFromResult(Job job, JsonNode resultNode) throws JobHandleException {
        if (resultNode == null || resultNode.isNull()) {
            throw new JobHandleException("Result payload is empty for daily plan job.");
        }

        User user = job.getUser();
        List<DailyPlanLLMResponseDto> planDtos = JsonUtils.fromJsonNodeToList(resultNode,
                                                                              DailyPlanLLMResponseDto.class);
        if (planDtos == null || planDtos.isEmpty()) {
            throw new JobHandleException("Failed to parse DailyPlanLLMResponseDto list from result.");
        }

        List<DailyPlan> dailyPlans = dailyPlanService.saveGeneratedDailyPlans(user, planDtos);
        List<DailyPlanSummaryDto> dailyPlanSummaryDtos = dailyPlans.stream().map(DailyPlanSummaryDto::new).toList();

        job.setResultMetadata(resultNode);
        job.markAsCompleted();
        jobRepository.save(job);

        webSocketService.sendJobCompletedUpdate(job, dailyPlanSummaryDtos);
    }

    private void completeTaskFromResult(Job job, JsonNode resultNode, DailyTaskType taskType)
            throws JobHandleException {
        if (resultNode == null || resultNode.isNull()) {
            throw new JobHandleException("Result payload is empty for task job.");
        }

        User user = job.getUser();
        LLMTaskGenerateContextDto taskGenerateContextDto = job.getMetadataAs(LLMTaskGenerateContextDto.class);
        if (taskGenerateContextDto == null) {
            throw new JobHandleException("Task generate context not found in job metadata.");
        }

        DailyTask task = null;
        if (taskType == DailyTaskType.READING) {
            ReadingTaskContent content = JsonUtils.fromJsonNode(resultNode, ReadingTaskContent.class);
            task = dailyTaskService.saveReadingTaskContent(user, taskGenerateContextDto.getTaskId(), content);
        } else if (taskType == DailyTaskType.QUIZ) {
            QuizTaskContent content = JsonUtils.fromJsonNode(resultNode, QuizTaskContent.class);
            task = dailyTaskService.saveQuizTaskContent(user, taskGenerateContextDto.getTaskId(), content);
        }

        if (task != null) {
            job.setResultMetadata(resultNode);
            job.markAsCompleted();
            jobRepository.save(job);

            webSocketService.sendJobCompletedUpdate(job, new DailyTaskDto(task));
        }
    }

    private void handleFailedJob(Job job, ErrorMessageDto error) {
        ErrorMessageDto errorMessageDto = (error != null) ? error : new ErrorMessageDto("Job processing failed",
                                                                                        "LLMJobError");
        job.setResultMetadataAs(errorMessageDto);
        job.markAsFailed();
        jobRepository.save(job);

        webSocketService.sendJobUpdate(job, JobStatus.FAILED, null);
    }
}
