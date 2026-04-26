package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.DailyPlanDto;
import com.quronest.quronest_backend.service.DailyPlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/daily-plan")
public class DailyPlanController {
    private final DailyPlanService dailyPlanService;

    public DailyPlanController(DailyPlanService dailyPlanService) {
        this.dailyPlanService = dailyPlanService;
    }

    @GetMapping("/generate-new")
    public List<DailyPlanDto> generateNewDailyPlan(@RequestParam LocalDate startDate) {
        return dailyPlanService.generateNewPlan(startDate);
    }
}
