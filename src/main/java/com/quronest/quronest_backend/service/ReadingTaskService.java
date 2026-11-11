package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.QuizConfig;
import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.dto.AnchorDto;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.QuizQuestionResultDto;
import com.quronest.quronest_backend.dto.QuizSubmitRequestDto;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.QuizPerformanceData;
import com.quronest.quronest_backend.model.QuizQuestion;
import com.quronest.quronest_backend.model.ReadingTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.DailyTaskLog;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.AnchorRepository;
import com.quronest.quronest_backend.repository.DailyTaskLogRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReadingTaskService {

    private final DailyTaskRepository dailyTaskRepository;
    private final DailyTaskLogRepository dailyTaskLogRepository;
    private final AnchorRepository anchorRepository;
    private final UserService userService;
    private final DailyTaskService dailyTaskService;
    private final QuizTaskService quizTaskService;
    private final QuronestConfig quronestConfig;

    public ReadingTaskService(DailyTaskRepository dailyTaskRepository,
                              DailyTaskLogRepository dailyTaskLogRepository,
                              AnchorRepository anchorRepository,
                              UserService userService,
                              DailyTaskService dailyTaskService,
                              QuizTaskService quizTaskService,
                              QuronestConfig quronestConfig) {
        this.dailyTaskRepository = dailyTaskRepository;
        this.dailyTaskLogRepository = dailyTaskLogRepository;
        this.anchorRepository = anchorRepository;
        this.userService = userService;
        this.dailyTaskService = dailyTaskService;
        this.quizTaskService = quizTaskService;
        this.quronestConfig = quronestConfig;
    }

    public DailyTaskDto getReadingTaskById(UUID taskId) {
        User user = userService.getAuthenticatedUser();
        DailyTask task = dailyTaskRepository.findByIdAndUserAndTaskType(taskId, user, DailyTaskType.READING);

        if (task == null) {
            throw new DailyTaskNotFoundException();
        }

        List<AnchorDto> anchorDtos = anchorRepository.findByReferenceId(taskId).stream()
                .map(AnchorDto::new)
                .toList();

        return new DailyTaskDto(task, anchorDtos);
    }

    public QuizSubmitResponseDto submitReadingTask(UUID taskId, QuizSubmitRequestDto requestDto) {
        User user = userService.getAuthenticatedUser();
        DailyTask task = dailyTaskRepository.findByIdAndUserAndTaskType(taskId, user, DailyTaskType.READING);

        if (task == null) {
            throw new DailyTaskNotFoundException();
        }

        ReadingTaskContent readingTaskContent = task.getContentAs(ReadingTaskContent.class);
        List<QuizQuestion> questionnaires = (readingTaskContent != null && readingTaskContent.getQuestionnaires() != null)
                ? readingTaskContent.getQuestionnaires()
                : Collections.emptyList();

        for (QuizQuestion question : questionnaires) {
            if (question.getMaxPoints() == null) {
                question.setMaxPoints(QuizConfig.MCQ_MAX_POINTS);
            }
        }

        DailyTaskLog taskLog = dailyTaskService.getOrCreateTaskLog(task, user);
        if (taskLog.getId() == null) {
            taskLog.setId(UUID.randomUUID());
        }
        UUID taskLogId = taskLog.getId();

        QuizSubmitRequestDto submitRequest = requestDto != null ? requestDto : new QuizSubmitRequestDto();
        List<QuizQuestionResultDto> resultQuestionDtos = quizTaskService.verifyMcq(questionnaires, submitRequest.getMcqAnswers());

        int totalQuestions = questionnaires.size();
        int maxTotalPoint = quizTaskService.getMaxTotalPoint(readingTaskContent);
        int totalPoints = resultQuestionDtos.stream()
                .mapToInt(QuizQuestionResultDto::getPoints)
                .sum();
        int totalCorrect = (int) resultQuestionDtos.stream()
                .filter(QuizQuestionResultDto::getIsCorrect)
                .count();

        double scorePercentage = maxTotalPoint > 0 ? ((double) totalPoints / maxTotalPoint) * 100.0 : 0.0;
        double passThreshold = 0.0;
        boolean passed = totalPoints > 0;

        int timeSpent = submitRequest.getTotalTimeSpent() != null ? submitRequest.getTotalTimeSpent() : 0;
        int calculatedProgress = passed ? 100 : (int) Math.round(scorePercentage);

        updateDailyTask(task, timeSpent, calculatedProgress, passed);

        UUID submissionId = UUID.randomUUID();
        QuizSubmitResponseDto responseDto = new QuizSubmitResponseDto(
                submissionId,
                taskLogId,
                task,
                totalQuestions,
                totalCorrect,
                totalPoints,
                maxTotalPoint,
                scorePercentage,
                passed,
                passThreshold,
                timeSpent,
                calculatedProgress,
                resultQuestionDtos
        );

        updateTaskLogAndPerformanceData(taskLog, task, responseDto);

        return responseDto;
    }

    private void updateDailyTask(DailyTask task, int timeSpent, int calculatedProgress, boolean passed) {
        int currentActualTime = task.getActualTimeSpent() != null ? task.getActualTimeSpent() : 0;
        task.setActualTimeSpent(currentActualTime + timeSpent);

        int currentProgress = task.getProgressPercent() != null ? task.getProgressPercent() : 0;
        if (passed) {
            task.setProgressPercent(100);
            task.setStatus(DailyTaskStatus.COMPLETED);
        } else {
            task.setProgressPercent(Math.max(currentProgress, calculatedProgress));
            if (task.getStatus() == DailyTaskStatus.PENDING) {
                task.setStatus(DailyTaskStatus.PARTIAL);
            }
        }
        dailyTaskRepository.save(task);
    }

    private void updateTaskLogAndPerformanceData(DailyTaskLog taskLog, DailyTask task,
                                                 QuizSubmitResponseDto responseDto) {
        taskLog.setAttempts((taskLog.getAttempts() != null ? taskLog.getAttempts() : 0) + 1);
        taskLog.setTotalTimeSpent((taskLog.getTotalTimeSpent() != null ? taskLog.getTotalTimeSpent() : 0) + responseDto.getTotalTimeSpent());

        double prevMax = taskLog.getMaxScore() != null ? taskLog.getMaxScore() : 0.0;
        double newMax = Math.max(prevMax, responseDto.getScorePercentage());
        taskLog.setMaxScore(newMax);
        taskLog.setAiScore(newMax);

        int prevProgress = taskLog.getProgressPercent() != null ? taskLog.getProgressPercent() : 0;
        taskLog.setProgressPercent(Math.max(prevProgress, responseDto.getCalculatedProgress()));
        taskLog.setLastAttemptAt(LocalDateTime.now());
        if (responseDto.getPassed() && taskLog.getCompletedAt() == null) {
            taskLog.setCompletedAt(LocalDateTime.now());
        }
        taskLog.setStatus(task.getStatus());

        QuizPerformanceData perfData = taskLog.getPerformanceDataAs(QuizPerformanceData.class);
        if (perfData == null) {
            perfData = new QuizPerformanceData();
        }
        if (perfData.getId() == null) {
            perfData.setId(UUID.randomUUID());
        }
        perfData.setTotalPoints(responseDto.getTotalPoints());
        perfData.setMaxPoints(responseDto.getMaxPoints());
        if (perfData.getSubmissions() == null) {
            perfData.setSubmissions(new ArrayList<>());
        }
        perfData.getSubmissions().add(responseDto);
        taskLog.setPerformanceDataJson(perfData);

        dailyTaskLogRepository.save(taskLog);
    }
}
