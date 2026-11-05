package com.quronest.quronest_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.dto.ErrorMessageDto;
import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.dto.llm.LLMJobResultDto;
import com.quronest.quronest_backend.dto.llm.LLMJobResultEventDto;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import com.quronest.quronest_backend.service.rabbit.JobConsumerService;
import com.quronest.quronest_backend.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LLMJobResultHandlerServiceTest {

    private LLMJobResultHandlerService llmJobResultHandlerService;
    private JobConsumerService jobConsumerService;

    private WebSocketService webSocketService;
    private JobRepository jobRepository;
    private UserJourneyService userJourneyService;
    private DailyPlanService dailyPlanService;
    private DailyTaskService dailyTaskService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        webSocketService = Mockito.mock(WebSocketService.class);
        jobRepository = Mockito.mock(JobRepository.class);
        userJourneyService = Mockito.mock(UserJourneyService.class);
        dailyPlanService = Mockito.mock(DailyPlanService.class);
        dailyTaskService = Mockito.mock(DailyTaskService.class);
        userService = Mockito.mock(UserService.class);

        llmJobResultHandlerService = new LLMJobResultHandlerService(
                webSocketService,
                jobRepository,
                userJourneyService,
                dailyPlanService,
                dailyTaskService,
                userService
        );

        jobConsumerService = new JobConsumerService(llmJobResultHandlerService);
    }

    @Test
    void testHandleCompletedUserSummaryJob() {
        UUID jobId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Job job = new Job();
        job.setId(jobId);
        job.setUser(user);
        job.setType(JobType.LLM_GENERATE_USER_SUMMARY);
        job.setStatus(JobStatus.PENDING);
        job.setRetries(0);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        UserGroupSummaryDto summaryDto = new UserGroupSummaryDto();
        summaryDto.setGroup(UserGroup.GROUP_A);
        summaryDto.setPhase(UserPhase.PHASE_1);
        summaryDto.setSummary("Great candidate summary");

        JsonNode resultNode = JsonUtils.toJsonNode(summaryDto);
        LLMJobResultDto resultDto = new LLMJobResultDto(
                jobId,
                JobType.LLM_GENERATE_USER_SUMMARY,
                "RESULT_SUBMITTED",
                1,
                2,
                resultNode,
                null
        );
        LLMJobResultEventDto eventDto = new LLMJobResultEventDto(
                UUID.randomUUID(),
                resultDto,
                LocalDateTime.now().toString()
        );

        jobConsumerService.consumeResult(eventDto);

        verify(userJourneyService, times(1)).saveUserJourneyFromSummary(eq(user), any(UserGroupSummaryDto.class));
        verify(jobRepository, times(1)).save(job);
        verify(webSocketService, times(1)).sendJobCompletedUpdate(eq(job), any(UserGroupSummaryDto.class));

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals(1, job.getRetries());
        assertNotNull(job.getCompletedAt());
        assertNotNull(job.getResultMetadata());
    }

    @Test
    void testHandleFailedJob() {
        UUID jobId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Job job = new Job();
        job.setId(jobId);
        job.setUser(user);
        job.setType(JobType.LLM_GENERATE_USER_SUMMARY);
        job.setStatus(JobStatus.PENDING);
        job.setRetries(0);
        job.setMaxRetries(2);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        ErrorMessageDto error = new ErrorMessageDto("Timeout calling model", "TimeoutException");
        LLMJobResultDto resultDto = new LLMJobResultDto(
                jobId,
                JobType.LLM_GENERATE_USER_SUMMARY,
                JobStatus.FAILED.name(),
                2,
                3,
                null,
                error
        );
        LLMJobResultEventDto eventDto = new LLMJobResultEventDto(
                UUID.randomUUID(),
                resultDto,
                LocalDateTime.now().toString()
        );

        jobConsumerService.consumeResult(eventDto);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertEquals(2, job.getRetries());
        assertNotNull(job.getFailedAt());
        verify(webSocketService, times(1)).sendJobUpdate(eq(job), eq(JobStatus.FAILED), isNull());
    }
}
