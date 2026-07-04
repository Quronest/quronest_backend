package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.DailyPlanDto;
import com.quronest.quronest_backend.dto.DailyTaskSummaryDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanGenerateLLMRequestDto;
import com.quronest.quronest_backend.dto.llm.DailyPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.DailyTaskPlanLLMResponseDto;
import com.quronest.quronest_backend.dto.llm.LLMUserContextDto;
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

@Service
public class DailyPlanService {
    private final LLMApiService llmApiService;
    private final UserService userService;
    private final LLMContextService llmContextService;
    private final UserJourneyService userJourneyService;
    private final JobProducerService jobProducerService;
    private final DailyPlanRepository dailyPlanRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final UserJourneyRepository userJourneyRepository;

    public DailyPlanService(LLMApiService llmApiService, UserService userService, LLMContextService llmContextService,
                            UserJourneyService userJourneyService, JobProducerService jobProducerService,
                            DailyPlanRepository dailyPlanRepository,
                            DailyTaskRepository dailyTaskRepository,
                            UserJourneyRepository userJourneyRepository) {
        this.llmApiService = llmApiService;
        this.userService = userService;
        this.llmContextService = llmContextService;
        this.userJourneyService = userJourneyService;
        this.jobProducerService = jobProducerService;
        this.dailyPlanRepository = dailyPlanRepository;
        this.dailyTaskRepository = dailyTaskRepository;
        this.userJourneyRepository = userJourneyRepository;
    }

    public JobStatusDto generateNextPlans() {
        User user = userService.getAuthenticatedUser();
        LLMUserContextDto userContextDto = llmContextService.getUserContext(user);
        DailyPlanGenerateLLMRequestDto llmRequestDto = new DailyPlanGenerateLLMRequestDto(userContextDto);

        // create new job and add to queue
        jobProducerService.verifyJobAlreadyExists(user, JobType.LLM_GENERATE_DAILY_PLAN,
                                                  "A Daily plan request is already in progress.");

        Job job = jobProducerService.createAndSendNewJob(user, JobType.LLM_GENERATE_DAILY_PLAN, llmRequestDto);

        return new JobStatusDto(job);
    }

    @Transactional
    public List<DailyPlan> completeNextDailyPlanGeneration(User user,
                                                                     DailyPlanGenerateLLMRequestDto llmRequestDto) {
        UserJourney journey = userJourneyService.getUserCurrentJourney(user);

        // create llm call
        List<DailyPlanLLMResponseDto> generatedPlans = llmApiService.generateDailyPlan(llmRequestDto);

        // add plan and task entries
        List<DailyPlan> dailyPlans = new ArrayList<>();
        LocalDate planDate = getNextPlanDate(journey);
        int dayNumber = getNextDayNumber(user, journey);

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

    private int getNextDayNumber(User user, UserJourney journey) {
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
}
