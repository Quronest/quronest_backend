package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.QuizConfig;
import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.QuizQuestionDto;
import com.quronest.quronest_backend.dto.QuizQuestionResultDto;
import com.quronest.quronest_backend.dto.QuizSubmitRequestDto;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
import com.quronest.quronest_backend.dto.QuizTaskContentDto;
import com.quronest.quronest_backend.dto.llm.QuizSaqValidationItemDto;
import com.quronest.quronest_backend.dto.llm.QuizSaqValidationResultItemDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.QuizPerformanceData;
import com.quronest.quronest_backend.model.QuizQuestion;
import com.quronest.quronest_backend.model.QuizTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.EvaluationClass;
import com.quronest.quronest_backend.model.enums.QuizQuestionType;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.DailyTaskLog;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.DailyTaskLogRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizTaskService {

    private final DailyTaskRepository dailyTaskRepository;
    private final DailyTaskLogRepository dailyTaskLogRepository;
    private final DailyTaskService dailyTaskService;
    private final UserService userService;
    private final QuronestConfig quronestConfig;
    private final LLMApiService llmApiService;

    public QuizTaskService(DailyTaskRepository dailyTaskRepository,
                           DailyTaskLogRepository dailyTaskLogRepository,
                           DailyTaskService dailyTaskService,
                           UserService userService,
                           QuronestConfig quronestConfig,
                           LLMApiService llmApiService) {
        this.dailyTaskRepository = dailyTaskRepository;
        this.dailyTaskLogRepository = dailyTaskLogRepository;
        this.dailyTaskService = dailyTaskService;
        this.userService = userService;
        this.quronestConfig = quronestConfig;
        this.llmApiService = llmApiService;
    }

    public DailyTaskDto getQuizTaskById(UUID taskId) {
        User user = userService.getAuthenticatedUser();
        DailyTask task = dailyTaskRepository.findByIdAndUserAndTaskType(taskId, user, DailyTaskType.QUIZ);

        if (task == null) {
            throw new DailyTaskNotFoundException();
        }

        DailyTaskDto dailyTaskDto = new DailyTaskDto(task);

        QuizTaskContent quizTaskContent = task.getContentAs(QuizTaskContent.class);
        if (quizTaskContent != null) {
            List<QuizQuestionDto> questionDtos = quizTaskContent.getQuestionnaires().stream()
                    .map(QuizQuestionDto::new)
                    .toList();
            dailyTaskDto.setContent(new QuizTaskContentDto(questionDtos, quizTaskContent.getMaxTotalPoint()));
        }

        return dailyTaskDto;
    }

    public List<QuizQuestionResultDto> verifyMcqQuestion(List<QuizQuestion> mcqQuestions,
                                                         Map<Integer, Integer> mcqAnswers) {
        if (mcqQuestions == null || mcqQuestions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, Integer> answers = mcqAnswers != null ? mcqAnswers : Collections.emptyMap();
        return mcqQuestions.stream()
                .map(q -> evaluateMcqQuestion(q, answers.get(q.getId())))
                .toList();
    }

    private QuizQuestionResultDto evaluateMcqQuestion(QuizQuestion question, Integer userAnswerOptionId) {
        boolean isCorrect = userAnswerOptionId != null
                && question.getSolutionOption() != null
                && userAnswerOptionId.equals(question.getSolutionOption().getId());
        EvaluationClass answerClass = isCorrect ? EvaluationClass.CORRECT : EvaluationClass.INCORRECT;
        int points = QuizConfig.MCQ_POINTS_MAP.getOrDefault(answerClass, 0);
        return new QuizQuestionResultDto(question, userAnswerOptionId, answerClass, points, isCorrect);
    }

    public List<QuizQuestionResultDto> verifySaqQuestion(List<QuizQuestion> saqQuestions,
                                                         Map<Integer, String> saqAnswers) {
        if (saqQuestions == null || saqQuestions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, String> answers = saqAnswers != null ? saqAnswers : Collections.emptyMap();
        List<QuizSaqValidationItemDto> validationItems = buildSaqValidationItems(saqQuestions, answers);
        List<QuizSaqValidationResultItemDto> validationResults = llmApiService.verifySaqQuestions(validationItems);
        Map<Integer, EvaluationClass> evalMap = extractEvaluationMap(validationResults);

        return saqQuestions.stream()
                .map(q -> evaluateSaqQuestion(q, answers.get(q.getId()), evalMap.get(q.getId())))
                .toList();
    }

    private List<QuizSaqValidationItemDto> buildSaqValidationItems(List<QuizQuestion> saqQuestions,
                                                                   Map<Integer, String> answers) {
        return saqQuestions.stream()
                .map(q -> new QuizSaqValidationItemDto(q, answers.getOrDefault(q.getId(), "")))
                .toList();
    }

    private Map<Integer, EvaluationClass> extractEvaluationMap(List<QuizSaqValidationResultItemDto> validationResults) {
        if (validationResults == null || validationResults.isEmpty()) {
            return Collections.emptyMap();
        }
        return validationResults.stream()
                .filter(res -> res.getQuestionId() != null && res.getClassifiedClass() != null)
                .collect(Collectors.toMap(
                        QuizSaqValidationResultItemDto::getQuestionId,
                        QuizSaqValidationResultItemDto::getClassifiedClass,
                        (existing, replacement) -> replacement
                ));
    }

    private QuizQuestionResultDto evaluateSaqQuestion(QuizQuestion question, String userAnswerText,
                                                      EvaluationClass answerClass) {
        EvaluationClass resolvedClass = answerClass != null ? answerClass : EvaluationClass.INCORRECT;
        int points = QuizConfig.SAQ_POINTS_MAP.getOrDefault(resolvedClass, 0);
        boolean isCorrect = resolvedClass == EvaluationClass.CORRECT;
        return new QuizQuestionResultDto(question, userAnswerText, resolvedClass, points, isCorrect);
    }

    public QuizSubmitResponseDto submitQuiz(UUID taskId, QuizSubmitRequestDto requestDto) {
        User user = userService.getAuthenticatedUser();
        DailyTask task = dailyTaskRepository.findByIdAndUserAndTaskType(taskId, user, DailyTaskType.QUIZ);

        if (task == null) {
            throw new DailyTaskNotFoundException();
        }

        QuizTaskContent quizTaskContent = task.getContentAs(QuizTaskContent.class);
        List<QuizQuestion> questionnaires = quizTaskContent != null
                ? quizTaskContent.getQuestionnaires()
                : Collections.emptyList();

        // 1. Separate into MCQ and SAQ questions while maintaining question id
        List<QuizQuestion> mcqQuestions = questionnaires.stream()
                .filter(q -> q.getType() != QuizQuestionType.SAQ)
                .toList();
        List<QuizQuestion> saqQuestions = questionnaires.stream()
                .filter(q -> q.getType() == QuizQuestionType.SAQ)
                .toList();

        // 2. Run getOrCreateTaskLog early to get taskLogId
        DailyTaskLog taskLog = dailyTaskService.getOrCreateTaskLog(task, user);
        if (taskLog.getId() == null) {
            taskLog.setId(UUID.randomUUID());
        }
        UUID taskLogId = taskLog.getId();

        // 3. Verify MCQ and SAQ questions
        QuizSubmitRequestDto submitRequest = requestDto != null ? requestDto : new QuizSubmitRequestDto();
        List<QuizQuestionResultDto> mcqResults = verifyMcqQuestion(mcqQuestions, submitRequest.getMcqAnswers());
        List<QuizQuestionResultDto> saqResults = verifySaqQuestion(saqQuestions, submitRequest.getSaqAnswers());

        List<QuizQuestionResultDto> resultQuestionDtos = combineResults(questionnaires, mcqResults, saqResults);

        // 4. Use max_total_point from QuizTaskContent directly (do NOT recalculate it)
        int maxTotalPoint = quizTaskContent != null && quizTaskContent.getMaxTotalPoint() != null
                ? quizTaskContent.getMaxTotalPoint()
                : 0;

        int totalQuestions = questionnaires.size();
        int totalPoints = resultQuestionDtos.stream()
                .mapToInt(QuizQuestionResultDto::getPoints)
                .sum();
        int totalCorrect = (int) resultQuestionDtos.stream()
                .filter(QuizQuestionResultDto::getIsCorrect)
                .count();

        double scorePercentage = maxTotalPoint > 0 ? ((double) totalPoints / maxTotalPoint) * 100.0 : 0.0;
        double passThreshold = quronestConfig.getQuizPassThreshold();
        boolean passed = scorePercentage >= passThreshold;

        int timeSpent = submitRequest.getTotalTimeSpent();
        int calculatedProgress = (int) Math.round(scorePercentage);

        // 5. Update DailyTask
        updateDailyTask(task, timeSpent, calculatedProgress, passed);

        // 6. Build response DTO
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

        // 7. Update DailyTaskLog and QuizPerformanceData (with UUID id and points)
        updateTaskLogAndPerformanceData(taskLog, task, responseDto);

        return responseDto;
    }

    private List<QuizQuestionResultDto> combineResults(List<QuizQuestion> questionnaires,
                                                       List<QuizQuestionResultDto> mcqResults,
                                                       List<QuizQuestionResultDto> saqResults) {
        Map<Integer, QuizQuestionResultDto> resultMap = new HashMap<>();
        for (QuizQuestionResultDto r : mcqResults) {
            resultMap.put(r.getId(), r);
        }
        for (QuizQuestionResultDto r : saqResults) {
            resultMap.put(r.getId(), r);
        }
        return questionnaires.stream()
                .map(q -> resultMap.get(q.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    private void updateDailyTask(DailyTask task, int timeSpent, int calculatedProgress, boolean passed) {
        task.setActualTimeSpent(task.getActualTimeSpent() + timeSpent);
        task.setProgressPercent(Math.max(task.getProgressPercent(), calculatedProgress));

        if (passed) {
            task.setStatus(DailyTaskStatus.COMPLETED);
        } else if (task.getStatus() == DailyTaskStatus.PENDING) {
            task.setStatus(DailyTaskStatus.PARTIAL);
        }
        dailyTaskRepository.save(task);
    }

    private void updateTaskLogAndPerformanceData(DailyTaskLog taskLog, DailyTask task,
                                                 QuizSubmitResponseDto responseDto) {
        taskLog.setAttempts(taskLog.getAttempts() + 1);
        taskLog.setTotalTimeSpent(taskLog.getTotalTimeSpent() + responseDto.getTotalTimeSpent());

        double newMax = Math.max(taskLog.getMaxScore(), responseDto.getScorePercentage());
        taskLog.setMaxScore(newMax);
        taskLog.setAiScore(newMax);

        taskLog.setProgressPercent(Math.max(taskLog.getProgressPercent(), responseDto.getCalculatedProgress()));
        taskLog.setLastAttemptAt(LocalDateTime.now());
        if (responseDto.getPassed() && taskLog.getCompletedAt() == null) {
            taskLog.setCompletedAt(LocalDateTime.now());
        }
        taskLog.setStatus(task.getStatus());

        // Update generic QuizPerformanceData (ensure UUID id is set, points stored, and append submission)
        QuizPerformanceData perfData = taskLog.getPerformanceDataAs(QuizPerformanceData.class);
        if (perfData == null) {
            perfData = new QuizPerformanceData();
        }
        if (perfData.getId() == null) {
            perfData.setId(UUID.randomUUID());
        }
        perfData.setTotalPoints(responseDto.getTotalPoints());
        perfData.setMaxPoints(responseDto.getMaxPoints());
        perfData.getSubmissions().add(responseDto);
        taskLog.setPerformanceDataJson(perfData);

        dailyTaskLogRepository.save(taskLog);
    }
}
