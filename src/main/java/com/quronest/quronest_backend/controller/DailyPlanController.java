package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.DailyPlanDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.service.DailyPlanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/daily-plans")
public class DailyPlanController {
    private final DailyPlanService dailyPlanService;

    public DailyPlanController(DailyPlanService dailyPlanService) {
        this.dailyPlanService = dailyPlanService;
    }

    @PostMapping("/generate-next-plans")
    public JobStatusDto generateNewDailyPlans() {
        return dailyPlanService.generateNextPlans();
    }

    @GetMapping("/by-date-range")
    public List<DailyPlanDto> getDailyPlansByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dailyPlanService.getDailyPlansByDateRange(startDate, endDate);
    }

    @GetMapping("/{planId}")
    public DailyPlanDto getDailyPlanById(@PathVariable UUID planId) {
        return dailyPlanService.getDailyPlanById(planId);
    }
}
