package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.DailyPlanDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.service.DailyPlanService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/daily-plans")
@Validated
public class DailyPlanController {
    private final DailyPlanService dailyPlanService;

    public DailyPlanController(DailyPlanService dailyPlanService) {
        this.dailyPlanService = dailyPlanService;
    }

    @PostMapping("/generate-next-plans")
    public JobStatusDto generateNewDailyPlans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false, defaultValue = "7") @Min(1) @Max(10) Integer limit) {
        return dailyPlanService.generateNextPlans(startDate, limit);
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
