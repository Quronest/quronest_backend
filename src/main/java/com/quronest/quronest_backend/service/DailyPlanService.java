package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.DailyPlanDto;
import com.quronest.quronest_backend.dto.DailyTaskSummaryDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanGenerateLLMRequestDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.DailyTaskPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.LLMUserContextDto;
import com.quronest.quronest_backend.exception.DailyPlanNotFoundException;
import com.quronest.quronest_backend.exception.InvalidLimitException;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.*;
import com.quronest.quronest_backend.repository.DailyPlanRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import com.quronest.quronest_backend.repository.UserJourneyRepository;
import com.quronest.quronest_backend.service.rabbit.JobProducerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DailyPlanService {
    private final UserService userService;
    private final LLMContextService llmContextService;
    private final UserJourneyService userJourneyService;
    private final JobProducerService jobProducerService;
    private final DailyPlanRepository dailyPlanRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final UserJourneyRepository userJourneyRepository;

    public DailyPlanService(UserService userService, LLMContextService llmContextService,
                            UserJourneyService userJourneyService, JobProducerService jobProducerService,
                            DailyPlanRepository dailyPlanRepository,
                            DailyTaskRepository dailyTaskRepository,
                            UserJourneyRepository userJourneyRepository) {
        this.userService = userService;
        this.llmContextService = llmContextService;
        this.userJourneyService = userJourneyService;
        this.jobProducerService = jobProducerService;
        this.dailyPlanRepository = dailyPlanRepository;
        this.dailyTaskRepository = dailyTaskRepository;
        this.userJourneyRepository = userJourneyRepository;
    }

    public JobStatusDto generateNextPlans(LocalDate startDate, Integer limit) {
        if (limit != null && (limit < 1 || limit > 10)) {
            throw new InvalidLimitException(1, 10);
        }

        User user = userService.getAuthenticatedUser();
        UserJourney journey = userJourneyService.getUserCurrentJourney(user);

        LocalDate planStartDate = startDate != null ? startDate : getNextPlanDate(journey);
        int planLimit = limit != null ? limit : 7;

        LLMUserContextDto userContextDto = llmContextService.getUserContext(journey);
        DailyPlanGenerateLLMRequestDto llmRequestDto = new DailyPlanGenerateLLMRequestDto(userContextDto,
                                                                                          planStartDate,
                                                                                          planLimit);

        // create new job and add to queue
        Job existedJob = jobProducerService.verifyAndGetExistedJob(user, JobType.LLM_GENERATE_DAILY_PLAN);
        if (existedJob != null) {
            return new JobStatusDto(existedJob);
        }

        Job job = jobProducerService.publishLLMJob(user, JobType.LLM_GENERATE_DAILY_PLAN, llmRequestDto);

        return new JobStatusDto(job);
    }

    @Transactional
    public List<DailyPlan> saveGeneratedDailyPlans(User user, List<DailyPlanLLMResponseDto> generatedPlans,
                                                   LocalDate startDate) {
        UserJourney journey = userJourneyService.getUserCurrentJourney(user);

        List<DailyPlan> dailyPlans = new ArrayList<>();
        LocalDate planDate = startDate != null ? startDate : getNextPlanDate(journey);
        int dayNumber = getNextDayNumber(user, journey, planDate);

        journey.setLastPlanGeneratedFrom(planDate);

        for (DailyPlanLLMResponseDto responseDto : generatedPlans) {
            DailyPlan dailyPlan = new DailyPlan(user, responseDto, planDate, dayNumber);
            dailyPlan = dailyPlanRepository.saveAndFlush(dailyPlan);

            // add task entries
            int totalExpectedTime = 0;
            for (DailyTaskPlanLLMResponseDto taskPlanLLMResponseDto : responseDto.getTasks()) {
                DailyTask dailyTask = new DailyTask(dailyPlan, user, taskPlanLLMResponseDto);
                dailyTaskRepository.save(dailyTask);

                totalExpectedTime += dailyTask.getExpectedTotalTime();
            }

            dailyPlan.setExpectedTotalTime(totalExpectedTime);
            dailyPlans.add(dailyPlan);
            planDate = planDate.plusDays(1);
            dayNumber += 1;
        }

        journey.setLastPlanGeneratedTill(planDate.minusDays(1));
        userJourneyRepository.save(journey);

        return dailyPlans;
    }

    private int getNextDayNumber(User user, UserJourney journey, LocalDate planDate) {
        DailyPlan previousPlan = dailyPlanRepository.findByUserAndGroupAndPhaseAndPlanDate(user, user.getGroup(),
                                                                                           user.getPhase(),
                                                                                           planDate.minusDays(1));
        if (previousPlan != null && previousPlan.getDayNumber() != null) {
            return previousPlan.getDayNumber() + 1;
        }

        if (journey.getLastPlanGeneratedTill() == null) {
            return 1;
        }

        DailyPlan lastDailyPlan = dailyPlanRepository.findByUserAndGroupAndPhaseAndPlanDate(user, user.getGroup(),
                                                                                            user.getPhase(),
                                                                                            journey.getLastPlanGeneratedTill());
        if (lastDailyPlan == null || lastDailyPlan.getDayNumber() == null) {
            return 1;
        }

        return lastDailyPlan.getDayNumber() + 1;
    }

    private LocalDate getNextPlanDate(UserJourney journey) {
        if (journey.getLastPlanGeneratedTill() == null) {
            return LocalDate.now();
        }

        return journey.getLastPlanGeneratedTill().plusDays(1);
    }

    public List<DailyPlanDto> getDailyPlansByDateRange(LocalDate startDate, LocalDate endDate) {
        User user = userService.getAuthenticatedUser();
        List<DailyPlan> dailyPlans = dailyPlanRepository.findByUserAndPlanDateBetweenOrderByPlanDateAsc(user, startDate,
                                                                                                        endDate);

        return dailyPlans.stream().map(this::getDailyPlan).toList();
    }

    public DailyPlanDto getDailyPlan(DailyPlan dailyPlan) {
        List<DailyTaskSummaryDto> taskSummaryDtos = dailyTaskRepository.findByPlanOrderByOrderAsc(dailyPlan).stream()
                .map(DailyTaskSummaryDto::new).toList();

        return new DailyPlanDto(dailyPlan, taskSummaryDtos);
    }

    public DailyPlanDto getDailyPlanById(UUID planId) {
        User user = userService.getAuthenticatedUser();
        DailyPlan plan = dailyPlanRepository.findByIdAndUser(planId, user);

        if (plan == null) {
            throw new DailyPlanNotFoundException();
        }

        return getDailyPlan(plan);
    }
}
