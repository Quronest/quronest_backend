package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.QuizConfig;
import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.dto.QuizQuestionResultDto;
import com.quronest.quronest_backend.dto.QuizSubmitRequestDto;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
import com.quronest.quronest_backend.dto.llm.QuizSaqValidationItemDto;
import com.quronest.quronest_backend.dto.llm.QuizSaqValidationResultItemDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.McqQuestionOption;
import com.quronest.quronest_backend.model.QuizPerformanceData;
import com.quronest.quronest_backend.model.QuizQuestion;
import com.quronest.quronest_backend.model.QuizTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.EvaluationClass;
import com.quronest.quronest_backend.model.enums.QuizQuestionType;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.DailyTaskLog;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.DailyTaskLogRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizTaskServiceSubmitTest {

    @Mock
    private DailyTaskRepository dailyTaskRepository;

    @Mock
    private DailyTaskLogRepository dailyTaskLogRepository;

    @Mock
    private DailyTaskService dailyTaskService;

    @Mock
    private UserService userService;

    @Mock
    private QuronestConfig quronestConfig;

    @Mock
    private LLMApiService llmApiService;

    @InjectMocks
    private QuizTaskService quizTaskService;

    private User testUser;
    private UUID taskId;
    private DailyPlan testPlan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(quronestConfig.getQuizPassThreshold()).thenReturn(30.0);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        taskId = UUID.randomUUID();

        testPlan = new DailyPlan();
        testPlan.setId(UUID.randomUUID());
    }

    private QuizQuestion createMcqQuestion(Integer id, String title, List<McqQuestionOption> options,
                                          McqQuestionOption solutionOption, String explanation) {
        return new QuizQuestion(id, QuizQuestionType.MCQ, title, options, solutionOption, null, explanation, QuizConfig.MCQ_MAX_POINTS);
    }

    private QuizQuestion createSaqQuestion(Integer id, String title, String solutionText, String explanation) {
        return new QuizQuestion(id, QuizQuestionType.SAQ, title, List.of(), null, solutionText, explanation, QuizConfig.SAQ_MAX_POINTS);
    }

    private QuizTaskContent createQuizTaskContent(List<QuizQuestion> questions) {
        int maxTotal = questions.stream()
                .mapToInt(q -> q.getMaxPoints() != null ? q.getMaxPoints() : 0)
                .sum();
        return new QuizTaskContent(questions, maxTotal);
    }

    @Test
    void verifyMcqQuestion_EvaluatesOptionsCorrectly() {
        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp 1");
        QuizQuestion q2 = createMcqQuestion(2, "Q2", List.of(opt1, opt2), opt2, "Exp 2");

        List<QuizQuestionResultDto> results = quizTaskService.verifyMcqQuestion(
                List.of(q1, q2),
                Map.of(1, 1, 2, 1) // Q1 correct (1), Q2 incorrect (1 instead of 2)
        );

        assertEquals(2, results.size());

        QuizQuestionResultDto r1 = results.get(0);
        assertEquals(1, r1.getId());
        assertTrue(r1.getIsCorrect());
        assertEquals(EvaluationClass.CORRECT, r1.getAnswerClass());
        assertEquals(1, r1.getPoints()); // 1 point for correct MCQ
        assertEquals(1, r1.getUserAnswerOptionId());

        QuizQuestionResultDto r2 = results.get(1);
        assertEquals(2, r2.getId());
        assertFalse(r2.getIsCorrect());
        assertEquals(EvaluationClass.INCORRECT, r2.getAnswerClass());
        assertEquals(0, r2.getPoints());
        assertEquals(1, r2.getUserAnswerOptionId());
    }

    @Test
    void verifySaqQuestion_EvaluatesViaLLMApiService() {
        QuizQuestion q1 = createSaqQuestion(1, "SAQ 1", "Sol 1", "Exp 1");
        QuizQuestion q2 = createSaqQuestion(2, "SAQ 2", "Sol 2", "Exp 2");

        when(llmApiService.verifySaqQuestions(any())).thenReturn(List.of(
                new QuizSaqValidationResultItemDto(1, EvaluationClass.CORRECT),
                new QuizSaqValidationResultItemDto(2, EvaluationClass.PARTIALLY_CORRECT)
        ));

        List<QuizQuestionResultDto> results = quizTaskService.verifySaqQuestion(
                List.of(q1, q2),
                Map.of(1, "Ans 1", 2, "Ans 2")
        );

        assertEquals(2, results.size());

        QuizQuestionResultDto r1 = results.get(0);
        assertEquals(1, r1.getId());
        assertTrue(r1.getIsCorrect());
        assertEquals(EvaluationClass.CORRECT, r1.getAnswerClass());
        assertEquals(2, r1.getPoints());
        assertEquals("Ans 1", r1.getUserAnswerText());

        QuizQuestionResultDto r2 = results.get(1);
        assertEquals(2, r2.getId());
        assertFalse(r2.getIsCorrect());
        assertEquals(EvaluationClass.PARTIALLY_CORRECT, r2.getAnswerClass());
        assertEquals(1, r2.getPoints());
        assertEquals("Ans 2", r2.getUserAnswerText());
    }

    @Test
    void submitQuiz_Passing_UpdatesTaskToCompletedAndUpdatesSingleLog() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);
        task.setActualTimeSpent(10);
        task.setProgressPercent(0);

        DailyTaskLog existingLog = new DailyTaskLog();
        UUID logId = UUID.randomUUID();
        existingLog.setId(logId);
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);
        existingLog.setAttempts(1);
        existingLog.setTotalTimeSpent(10);
        existingLog.setMaxScore(20.0);
        existingLog.setProgressPercent(20);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp 1");
        QuizQuestion q2 = createMcqQuestion(2, "Q2", List.of(opt1, opt2), opt2, "Exp 2");

        QuizTaskContent content = createQuizTaskContent(List.of(q1, q2));
        task.setContentJson(content);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // User answers Q1 correctly (1 pt), Q2 incorrectly (0 pt) -> 1/2 points = 50% >= 30%
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 1, 2, 1), 15);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(logId, response.getTaskLogId());
        assertEquals(taskId, response.getTaskId());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(1, response.getTotalCorrect());
        assertEquals(1, response.getTotalPoints());
        assertEquals(2, response.getMaxPoints());
        assertEquals(50.0, response.getScorePercentage());
        assertEquals(50, response.getCalculatedProgress());
        assertTrue(response.getPassed());
        assertEquals(DailyTaskStatus.COMPLETED, response.getStatus());

        // Verify DailyTask was updated
        assertEquals(25, task.getActualTimeSpent()); // 10 + 15
        assertEquals(50, task.getProgressPercent());
        assertEquals(DailyTaskStatus.COMPLETED, task.getStatus());
        verify(dailyTaskRepository).save(task);

        // Verify single DailyTaskLog was updated
        ArgumentCaptor<DailyTaskLog> logCaptor = ArgumentCaptor.forClass(DailyTaskLog.class);
        verify(dailyTaskLogRepository).save(logCaptor.capture());
        DailyTaskLog savedLog = logCaptor.getValue();

        assertEquals(2, savedLog.getAttempts());
        assertEquals(25, savedLog.getTotalTimeSpent()); // 10 + 15
        assertEquals(50.0, savedLog.getMaxScore());
        assertEquals(50.0, savedLog.getAiScore());
        assertEquals(50, savedLog.getProgressPercent());
        assertEquals(DailyTaskStatus.COMPLETED, savedLog.getStatus());
        assertNotNull(savedLog.getCompletedAt());
        assertNotNull(savedLog.getLastAttemptAt());

        // Verify performance data has submission, an id, and stored points
        QuizPerformanceData perfData = savedLog.getPerformanceDataAs(QuizPerformanceData.class);
        assertNotNull(perfData);
        assertNotNull(perfData.getId());
        assertEquals(1, perfData.getTotalPoints());
        assertEquals(2, perfData.getMaxPoints());
        assertEquals(1, perfData.getSubmissions().size());
        assertEquals(50.0, perfData.getSubmissions().get(0).getScorePercentage());
    }

    @Test
    void submitQuiz_MixedMcqAndSaq_EvaluatesBothCorrectly() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);

        DailyTaskLog existingLog = new DailyTaskLog();
        UUID logId = UUID.randomUUID();
        existingLog.setId(logId);
        existingLog.setTask(task);
        existingLog.setUser(testUser);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        QuizQuestion q1 = createMcqQuestion(1, "MCQ 1", List.of(opt1, opt2), opt1, "Exp 1");
        QuizQuestion q2 = createSaqQuestion(2, "SAQ 1", "Polymorphism", "Exp 2");

        task.setContentJson(createQuizTaskContent(List.of(q1, q2)));

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);
        when(llmApiService.verifySaqQuestions(any())).thenReturn(List.of(
                new QuizSaqValidationResultItemDto(2, EvaluationClass.CORRECT)
        ));

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto();
        requestDto.setMcqAnswers(Map.of(1, 1)); // Correct MCQ answer (1 pt)
        requestDto.setSaqAnswers(Map.of(2, "polymorphism")); // SAQ answer (2 pts)
        requestDto.setTotalTimeSpent(20);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(logId, response.getTaskLogId());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(2, response.getTotalCorrect());
        assertEquals(3, response.getTotalPoints()); // 1 + 2 = 3
        assertEquals(3, response.getMaxPoints());   // 1 + 2 = 3
        assertEquals(100.0, response.getScorePercentage());
        assertTrue(response.getPassed());

        assertEquals(2, response.getQuestions().size());
        QuizQuestionResultDto res1 = response.getQuestions().get(0);
        assertEquals(QuizQuestionType.MCQ, res1.getType());
        assertTrue(res1.getIsCorrect());
        assertEquals(EvaluationClass.CORRECT, res1.getAnswerClass());
        assertEquals(1, res1.getPoints()); // 1 point for MCQ
        assertEquals(1, res1.getUserAnswerOptionId());
        assertEquals(opt1, res1.getSolutionOption());

        QuizQuestionResultDto res2 = response.getQuestions().get(1);
        assertEquals(QuizQuestionType.SAQ, res2.getType());
        assertTrue(res2.getIsCorrect());
        assertEquals(EvaluationClass.CORRECT, res2.getAnswerClass());
        assertEquals(2, res2.getPoints()); // 2 points for SAQ
        assertEquals("polymorphism", res2.getUserAnswerText());
        assertEquals("Polymorphism", res2.getSolutionText());

        // Verify blocking LLM call made with SAQ validation item
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QuizSaqValidationItemDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(llmApiService).verifySaqQuestions(captor.capture());

        List<QuizSaqValidationItemDto> items = captor.getValue();
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuestionId());
        assertEquals("SAQ 1", items.get(0).getQuestionText());
        assertEquals("Polymorphism", items.get(0).getSolutionText());
        assertEquals("polymorphism", items.get(0).getAnswerText());
    }

    @Test
    void submitQuiz_MixedMcqAndSaq_PartiallyCorrectSaq() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setId(UUID.randomUUID());
        existingLog.setTask(task);
        existingLog.setUser(testUser);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        QuizQuestion q1 = createMcqQuestion(1, "MCQ 1", List.of(opt1, opt2), opt1, "Exp 1");
        QuizQuestion q2 = createSaqQuestion(2, "SAQ 1", "Encapsulation hides data", "Exp 2");

        task.setContentJson(createQuizTaskContent(List.of(q1, q2)));

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);
        when(llmApiService.verifySaqQuestions(any())).thenReturn(List.of(
                new QuizSaqValidationResultItemDto(2, EvaluationClass.PARTIALLY_CORRECT)
        ));

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto();
        requestDto.setMcqAnswers(Map.of(1, 1)); // Correct MCQ (1 pt)
        requestDto.setSaqAnswers(Map.of(2, "hides data")); // Partially correct SAQ (1 pt)
        requestDto.setTotalTimeSpent(10);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertEquals(2, response.getTotalQuestions());
        assertEquals(1, response.getTotalCorrect());
        assertEquals(2, response.getTotalPoints()); // 1 + 1 = 2
        assertEquals(3, response.getMaxPoints());   // 1 + 2 = 3
        assertEquals(66.67, response.getScorePercentage(), 0.01); // 2/3 = 66.67%
        assertTrue(response.getPassed());

        QuizQuestionResultDto res2 = response.getQuestions().get(1);
        assertEquals(EvaluationClass.PARTIALLY_CORRECT, res2.getAnswerClass());
        assertEquals(1, res2.getPoints());
        assertFalse(res2.getIsCorrect());
    }

    @Test
    void submitQuiz_Failing_UpdatesTaskToPartial() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);
        task.setActualTimeSpent(0);
        task.setProgressPercent(0);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setId(UUID.randomUUID());
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);
        existingLog.setAttempts(0);
        existingLog.setTotalTimeSpent(0);
        existingLog.setMaxScore(0.0);
        existingLog.setProgressPercent(0);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp");
        QuizQuestion q2 = createMcqQuestion(2, "Q2", List.of(opt1, opt2), opt1, "Exp");
        QuizQuestion q3 = createMcqQuestion(3, "Q3", List.of(opt1, opt2), opt1, "Exp");
        QuizQuestion q4 = createMcqQuestion(4, "Q4", List.of(opt1, opt2), opt1, "Exp");

        task.setContentJson(createQuizTaskContent(List.of(q1, q2, q3, q4)));

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // User answers 0 questions correctly -> 0% < 30%
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 2, 2, 2, 3, 2, 4, 2), 20);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertEquals(0, response.getTotalPoints());
        assertEquals(4, response.getMaxPoints()); // 4 * 1 = 4
        assertEquals(0.0, response.getScorePercentage());
        assertFalse(response.getPassed());
        assertEquals(DailyTaskStatus.PARTIAL, response.getStatus());

        assertEquals(DailyTaskStatus.PARTIAL, task.getStatus());
        verify(dailyTaskRepository).save(task);

        ArgumentCaptor<DailyTaskLog> logCaptor = ArgumentCaptor.forClass(DailyTaskLog.class);
        verify(dailyTaskLogRepository).save(logCaptor.capture());
        DailyTaskLog savedLog = logCaptor.getValue();

        assertEquals(1, savedLog.getAttempts());
        assertEquals(20, savedLog.getTotalTimeSpent());
        assertEquals(0.0, savedLog.getMaxScore());
    }

    @Test
    void submitQuiz_MultipleSubmissions_PreservesMaxScoreAndAppendsSubmissions() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.COMPLETED);
        task.setActualTimeSpent(30);
        task.setProgressPercent(80);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setId(UUID.randomUUID());
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);
        existingLog.setAttempts(1);
        existingLog.setTotalTimeSpent(30);
        existingLog.setMaxScore(80.0);
        existingLog.setProgressPercent(80);

        QuizSubmitResponseDto prevSub = new QuizSubmitResponseDto();
        prevSub.setScorePercentage(80.0);
        QuizPerformanceData existingPerf = new QuizPerformanceData(UUID.randomUUID(), 0, 0, new ArrayList<>(List.of(prevSub)));
        existingLog.setPerformanceDataJson(existingPerf);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");
        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp");

        task.setContentJson(createQuizTaskContent(List.of(q1)));

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // Attempt 2: User answers incorrectly (0%)
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 2), 10);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertEquals(0.0, response.getScorePercentage());

        // Max score remains 80.0, attempts becomes 2
        assertEquals(80.0, existingLog.getMaxScore());
        assertEquals(2, existingLog.getAttempts());
        assertEquals(40, existingLog.getTotalTimeSpent());

        QuizPerformanceData perfData = existingLog.getPerformanceDataAs(QuizPerformanceData.class);
        assertNotNull(perfData);
        assertEquals(2, perfData.getSubmissions().size());
        assertEquals(80.0, perfData.getSubmissions().get(0).getScorePercentage());
        assertEquals(0.0, perfData.getSubmissions().get(1).getScorePercentage());
    }

    @Test
    void submitQuiz_AlreadyCompletedTask_DoesNotOverwriteCompletedStatus() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.COMPLETED);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setId(UUID.randomUUID());
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");
        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp");

        task.setContentJson(createQuizTaskContent(List.of(q1)));

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // Failing submission
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 2), 5);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        // Task status should remain COMPLETED
        assertEquals(DailyTaskStatus.COMPLETED, task.getStatus());
        assertEquals(DailyTaskStatus.COMPLETED, response.getStatus());
    }

    @Test
    void submitQuiz_NullContent_HandlesGracefully() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);
        task.setContent(null);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setId(UUID.randomUUID());
        existingLog.setTask(task);
        existingLog.setUser(testUser);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(), 5);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertEquals(0, response.getTotalQuestions());
        assertEquals(0, response.getTotalCorrect());
        assertEquals(0, response.getTotalPoints());
        assertEquals(0, response.getMaxPoints());
        assertEquals(0.0, response.getScorePercentage());
        assertFalse(response.getPassed());
    }

    @Test
    void submitQuiz_NullAnswersAndNullTimeSpent_HandlesGracefully() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setId(UUID.randomUUID());
        existingLog.setTask(task);
        existingLog.setUser(testUser);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(null, null);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertEquals(0, response.getTotalTimeSpent());
    }

    @Test
    void submitQuiz_NotFound_ThrowsException() {
        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(null);

        assertThrows(DailyTaskNotFoundException.class, () -> quizTaskService.submitQuiz(taskId, new QuizSubmitRequestDto()));
    }

    @Test
    void submitQuiz_UsesStoredMaxTotalPointDirectlyWithoutRecalculating() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setId(UUID.randomUUID());
        existingLog.setTask(task);
        existingLog.setUser(testUser);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1), opt1, "Exp");

        // Manually override maxTotalPoint to a custom stored value (e.g. 10 instead of 1)
        QuizTaskContent content = new QuizTaskContent(List.of(q1), 10);
        task.setContentJson(content);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 1), 5); // 1 point scored

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertEquals(1, response.getTotalPoints());
        assertEquals(10, response.getMaxPoints()); // Used stored max_total_point directly
        assertEquals(10.0, response.getScorePercentage()); // 1 / 10 = 10%
    }
}
