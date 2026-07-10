package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.service.DailyTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/tasks")
public class DailyTaskController {
    private final DailyTaskService dailyTaskService;

    public DailyTaskController(DailyTaskService dailyTaskService) {
        this.dailyTaskService = dailyTaskService;
    }

    @PostMapping("/{taskId}/generate")
    public JobStatusDto createTaskGenerateJob(@PathVariable UUID taskId) {
        return dailyTaskService.createTaskGenerateJob(taskId);
    }

    @GetMapping("/{taskId}")
    public DailyTaskDto getDailyTaskById(@PathVariable UUID taskId) {
        return dailyTaskService.getTaskById(taskId);
    }
}
