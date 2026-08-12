package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.dto.QuizSubmitRequestDto;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
import com.quronest.quronest_backend.service.DailyTaskService;
import com.quronest.quronest_backend.service.QuizTaskService;
import com.quronest.quronest_backend.service.ReadingTaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/tasks")
public class DailyTaskController {
    private final DailyTaskService dailyTaskService;
    private final QuizTaskService quizTaskService;
    private final ReadingTaskService readingTaskService;

    public DailyTaskController(DailyTaskService dailyTaskService,
                               QuizTaskService quizTaskService,
                               ReadingTaskService readingTaskService) {
        this.dailyTaskService = dailyTaskService;
        this.quizTaskService = quizTaskService;
        this.readingTaskService = readingTaskService;
    }

    @PostMapping("/{taskId}/generate")
    public JobStatusDto createTaskGenerateJob(@PathVariable UUID taskId) {
        return dailyTaskService.createTaskGenerateJob(taskId);
    }

    @GetMapping("/{taskId}")
    public DailyTaskDto getDailyTaskById(@PathVariable UUID taskId) {
        return dailyTaskService.getTaskById(taskId);
    }

    @GetMapping("/{taskId}/reading")
    public DailyTaskDto getReadingTaskById(@PathVariable UUID taskId) {
        return readingTaskService.getReadingTaskById(taskId);
    }

    @GetMapping("/{taskId}/quiz")
    public DailyTaskDto getQuizTaskById(@PathVariable UUID taskId) {
        return quizTaskService.getQuizTaskById(taskId);
    }

    @PostMapping("/{taskId}/quiz/submit")
    public QuizSubmitResponseDto submitQuiz(@PathVariable UUID taskId,
                                             @Valid @RequestBody QuizSubmitRequestDto requestDto) {
        return quizTaskService.submitQuiz(taskId, requestDto);
    }
}
