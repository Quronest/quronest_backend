package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.DailyPlanDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyPlanService {
    private final LLMApiService llmApiService;
    private final UserService userService;

    public DailyPlanService(LLMApiService llmApiService, UserService userService) {
        this.llmApiService = llmApiService;
        this.userService = userService;
    }

    public List<DailyPlanDto> generateNewPlan(LocalDate startDate) {
        return null;
    }
}
