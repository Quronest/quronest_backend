package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.*;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.McqQuestion;
import com.quronest.quronest_backend.model.QuizPerformanceData;
import com.quronest.quronest_backend.model.QuizTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.DailyTaskLog;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.DailyTaskLogRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import com.quronest.quronest_backend.config.QuronestConfig;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QuizTaskService {

    private final DailyTaskRepository dailyTaskRepository;
    private final DailyTaskLogRepository dailyTaskLogRepository;
    private final DailyTaskService dailyTaskService;
    private final UserService userService;
    private final QuronestConfig quronestConfig;

    public QuizTaskService(DailyTaskRepository dailyTaskRepository,
                           DailyTaskLogRepository dailyTaskLogRepository,
                           DailyTaskService dailyTaskService,
                           UserService userService,
                           QuronestConfig quronestConfig) {
        this.dailyTaskRepository = dailyTaskRepository;
        this.dailyTaskLogRepository = dailyTaskLogRepository;
        this.dailyTaskService = dailyTaskService;
        this.userService = userService;
        this.quronestConfig = quronestConfig;
    }

    public DailyTaskDto getQuizTaskById(UUID taskId) {
        User user = userService.getAuthenticatedUser();
        DailyTask task = dailyTaskRepository.findByIdAndUserAndTaskType(taskId, user, DailyTaskType.QUIZ);

        if (task == null) {
            throw new DailyTaskNotFoundException();
        }

        DailyTaskDto dailyTaskDto = new DailyTaskDto(task);

        if (task.getContent() != null) {
            QuizTaskContent quizTaskContent = task.getContentAs(QuizTaskContent.class);
            if (quizTaskContent != null && quizTaskContent.getQuestionnaires() != null) {
                List<QuizQuestionDto> questionDtos = quizTaskContent.getQuestionnaires().stream()
                        .map(QuizQuestionDto::new)
                        .toList();
                dailyTaskDto.setContent(new QuizTaskContentDto(questionDtos));
            }
        }

        return dailyTaskDto;
    }

    public QuizSubmitResponseDto submitQuiz(UUID taskId, QuizSubmitRequestDto requestDto) {
        User user = userService.getAuthenticatedUser();
        DailyTask task = dailyTaskRepository.findByIdAndUserAndTaskType(taskId, user, DailyTaskType.QUIZ);

        if (task == null) {
            throw new DailyTaskNotFoundException();
        }

        QuizTaskContent quizTaskContent = task.getContent() != null ? task.getContentAs(QuizTaskContent.class) : null;
        List<McqQuestion> questionnaires = (quizTaskContent != null && quizTaskContent.getQuestionnaires() != null)
                ? quizTaskContent.getQuestionnaires()
                : new ArrayList<>();

        Map<Integer, Integer> userAnswers = requestDto.getAnswers() != null ? requestDto.getAnswers() : Map.of();

        List<QuizQuestionResultDto> resultQuestionDtos = new ArrayList<>();
        int totalQuestions = questionnaires.size();
        int totalCorrect = 0;

        for (McqQuestion question : questionnaires) {
            Integer userAnswerOptionId = userAnswers.get(question.getId());
            boolean isCorrect = false;
            if (userAnswerOptionId != null && question.getSolution() != null
                    && userAnswerOptionId.equals(question.getSolution().getId())) {
                isCorrect = true;
                totalCorrect++;
            }
            resultQuestionDtos.add(new QuizQuestionResultDto(question, userAnswerOptionId, isCorrect));
        }

        double scorePercentage = totalQuestions > 0 ? ((double) totalCorrect / totalQuestions) * 100.0 : 0.0;
        double passThreshold = quronestConfig.getQuizPassThreshold() != null ? quronestConfig.getQuizPassThreshold() : 30.0;
        boolean passed = scorePercentage >= passThreshold;

        // Update DailyTask
        int timeSpent = requestDto.getTotalTimeSpent() != null ? requestDto.getTotalTimeSpent() : 0;
        int currentActualTime = task.getActualTimeSpent() != null ? task.getActualTimeSpent() : 0;
        task.setActualTimeSpent(currentActualTime + timeSpent);

        int calculatedProgress = (int) Math.round(scorePercentage);
        int currentProgress = task.getProgressPercent() != null ? task.getProgressPercent() : 0;
        task.setProgressPercent(Math.max(currentProgress, calculatedProgress));

        if (passed) {
            task.setStatus(DailyTaskStatus.COMPLETED);
        } else if (task.getStatus() == DailyTaskStatus.PENDING) {
            task.setStatus(DailyTaskStatus.PARTIAL);
        }
        dailyTaskRepository.save(task);

        // Create QuizSubmitResponseDto
        QuizSubmitResponseDto responseDto = new QuizSubmitResponseDto(
                task, totalQuestions, totalCorrect, scorePercentage, passed, passThreshold, timeSpent, resultQuestionDtos
        );

        // Get or Create single DailyTaskLog (1:1 per task)
        DailyTaskLog taskLog = dailyTaskService.getOrCreateTaskLog(task, user);

        taskLog.setAttempts((taskLog.getAttempts() != null ? taskLog.getAttempts() : 0) + 1);
        taskLog.setTotalTimeSpent((taskLog.getTotalTimeSpent() != null ? taskLog.getTotalTimeSpent() : 0) + timeSpent);

        double prevMax = taskLog.getMaxScore() != null ? taskLog.getMaxScore() : 0.0;
        double newMax = Math.max(prevMax, scorePercentage);
        taskLog.setMaxScore(newMax);
        taskLog.setAiScore(newMax);

        taskLog.setProgressPercent(Math.max(taskLog.getProgressPercent() != null ? taskLog.getProgressPercent() : 0, calculatedProgress));
        taskLog.setLastAttemptAt(LocalDateTime.now());
        if (passed && taskLog.getCompletedAt() == null) {
            taskLog.setCompletedAt(LocalDateTime.now());
        }
        taskLog.setStatus(task.getStatus());

        // Update generic QuizPerformanceData (append submission)
        QuizPerformanceData perfData = taskLog.getPerformanceDataAs(QuizPerformanceData.class);
        if (perfData == null) {
            perfData = new QuizPerformanceData();
        }
        perfData.getSubmissions().add(responseDto);
        taskLog.setPerformanceDataJson(perfData);

        dailyTaskLogRepository.save(taskLog);

        return responseDto;
    }
}
