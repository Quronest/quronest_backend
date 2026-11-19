package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanGenerateLLMRequestDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.DailyTaskPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.LLMUserContextDto;
import com.quronest.quronest_backend.exception.InvalidLimitException;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.model.table.UserJourney;
import com.quronest.quronest_backend.repository.DailyPlanRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import com.quronest.quronest_backend.repository.UserJourneyRepository;
import com.quronest.quronest_backend.service.rabbit.JobProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyPlanServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private LLMContextService llmContextService;

    @Mock
    private UserJourneyService userJourneyService;

    @Mock
    private JobProducerService jobProducerService;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @Mock
    private DailyTaskRepository dailyTaskRepository;

    @Mock
    private UserJourneyRepository userJourneyRepository;

    private DailyPlanService dailyPlanService;

    private User user;
    private UserJourney journey;

    @BeforeEach
    void setUp() {
        dailyPlanService = new DailyPlanService(
                userService,
                llmContextService,
                userJourneyService,
                jobProducerService,
                dailyPlanRepository,
                dailyTaskRepository,
                userJourneyRepository
        );

        user = new User();
        user.setId(UUID.randomUUID());

        journey = new UserJourney();
        journey.setId(UUID.randomUUID());
        journey.setUser(user);
        journey.setGroup(UserGroup.GROUP_A);
        journey.setPhase(UserPhase.PHASE_1);
    }

    @Test
    void generateNextPlans_withExplicitStartDateAndLimit() {
        LocalDate startDate = LocalDate.of(2026, 12, 1);
        int limit = 5;

        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(userJourneyService.getUserCurrentJourney(user)).thenReturn(journey);
        when(jobProducerService.verifyAndGetExistedJob(user, JobType.LLM_GENERATE_DAILY_PLAN)).thenReturn(null);

        LLMUserContextDto userContext = new LLMUserContextDto();
        when(llmContextService.getUserContext(journey)).thenReturn(userContext);

        Job createdJob = new Job();
        createdJob.setId(UUID.randomUUID());
        when(jobProducerService.publishLLMJob(eq(user), eq(JobType.LLM_GENERATE_DAILY_PLAN), any(DailyPlanGenerateLLMRequestDto.class)))
                .thenReturn(createdJob);

        JobStatusDto result = dailyPlanService.generateNextPlans(startDate, limit);

        assertThat(result).isNotNull();
        assertThat(result.getJobId()).isEqualTo(createdJob.getId());

        ArgumentCaptor<DailyPlanGenerateLLMRequestDto> captor = ArgumentCaptor.forClass(DailyPlanGenerateLLMRequestDto.class);
        verify(jobProducerService).publishLLMJob(eq(user), eq(JobType.LLM_GENERATE_DAILY_PLAN), captor.capture());

        DailyPlanGenerateLLMRequestDto publishedRequest = captor.getValue();
        assertThat(publishedRequest.getStartDate()).isEqualTo(startDate);
        assertThat(publishedRequest.getLimit()).isEqualTo(5);
    }

    @Test
    void generateNextPlans_withNullStartDate_defaultsToJourneyNextPlanDate() {
        journey.setLastPlanGeneratedTill(LocalDate.of(2026, 11, 20));

        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(userJourneyService.getUserCurrentJourney(user)).thenReturn(journey);
        when(jobProducerService.verifyAndGetExistedJob(user, JobType.LLM_GENERATE_DAILY_PLAN)).thenReturn(null);

        LLMUserContextDto userContext = new LLMUserContextDto();
        when(llmContextService.getUserContext(journey)).thenReturn(userContext);

        Job createdJob = new Job();
        createdJob.setId(UUID.randomUUID());
        when(jobProducerService.publishLLMJob(eq(user), eq(JobType.LLM_GENERATE_DAILY_PLAN), any(DailyPlanGenerateLLMRequestDto.class)))
                .thenReturn(createdJob);

        dailyPlanService.generateNextPlans(null, null);

        ArgumentCaptor<DailyPlanGenerateLLMRequestDto> captor = ArgumentCaptor.forClass(DailyPlanGenerateLLMRequestDto.class);
        verify(jobProducerService).publishLLMJob(eq(user), eq(JobType.LLM_GENERATE_DAILY_PLAN), captor.capture());

        DailyPlanGenerateLLMRequestDto publishedRequest = captor.getValue();
        assertThat(publishedRequest.getStartDate()).isEqualTo(LocalDate.of(2026, 11, 21));
        assertThat(publishedRequest.getLimit()).isEqualTo(7);
    }

    @Test
    void generateNextPlans_withInvalidLimit_throwsException() {
        assertThatThrownBy(() -> dailyPlanService.generateNextPlans(LocalDate.now(), 0))
                .isInstanceOf(InvalidLimitException.class);

        assertThatThrownBy(() -> dailyPlanService.generateNextPlans(LocalDate.now(), 11))
                .isInstanceOf(InvalidLimitException.class);
    }

    @Test
    void saveGeneratedDailyPlans_setsDatesFromStartDateBasedOnPlanListSize() {
        LocalDate startDate = LocalDate.of(2026, 12, 10);
        when(userJourneyService.getUserCurrentJourney(user)).thenReturn(journey);
        when(dailyPlanRepository.saveAndFlush(any(DailyPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyTaskPlanLLMResponseDto task1 = new DailyTaskPlanLLMResponseDto();
        task1.setTitle("Task 1");
        task1.setType(DailyTaskType.READING);
        task1.setExpectedTotalMinutes(30);

        DailyPlanLLMResponseDto plan1 = new DailyPlanLLMResponseDto();
        plan1.setTitle("Day 1 Plan");
        plan1.setDescription("Desc 1");
        plan1.setTasks(List.of(task1));

        DailyPlanLLMResponseDto plan2 = new DailyPlanLLMResponseDto();
        plan2.setTitle("Day 2 Plan");
        plan2.setDescription("Desc 2");
        plan2.setTasks(List.of(task1));

        DailyPlanLLMResponseDto plan3 = new DailyPlanLLMResponseDto();
        plan3.setTitle("Day 3 Plan");
        plan3.setDescription("Desc 3");
        plan3.setTasks(List.of(task1));

        List<DailyPlanLLMResponseDto> generatedPlans = List.of(plan1, plan2, plan3);

        List<DailyPlan> savedPlans = dailyPlanService.saveGeneratedDailyPlans(user, generatedPlans, startDate);

        assertThat(savedPlans).hasSize(3);
        assertThat(savedPlans.get(0).getPlanDate()).isEqualTo(LocalDate.of(2026, 12, 10));
        assertThat(savedPlans.get(1).getPlanDate()).isEqualTo(LocalDate.of(2026, 12, 11));
        assertThat(savedPlans.get(2).getPlanDate()).isEqualTo(LocalDate.of(2026, 12, 12));

        assertThat(journey.getLastPlanGeneratedFrom()).isEqualTo(LocalDate.of(2026, 12, 10));
        assertThat(journey.getLastPlanGeneratedTill()).isEqualTo(LocalDate.of(2026, 12, 12));
        verify(userJourneyRepository).save(journey);
    }
}
