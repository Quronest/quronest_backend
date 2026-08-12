package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.dto.QuizSubmitRequestDto;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.McqQuestion;
import com.quronest.quronest_backend.model.McqQuestionOption;
import com.quronest.quronest_backend.model.QuizPerformanceData;
import com.quronest.quronest_backend.model.QuizTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
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
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);
        existingLog.setAttempts(1);
        existingLog.setTotalTimeSpent(10);
        existingLog.setMaxScore(20.0);
        existingLog.setProgressPercent(20);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        McqQuestion q1 = new McqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp 1");
        McqQuestion q2 = new McqQuestion(2, "Q2", List.of(opt1, opt2), opt2, "Exp 2");

        QuizTaskContent content = new QuizTaskContent(List.of(q1, q2));
        task.setContentJson(content);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // User answers Q1 correctly (option 1), Q2 incorrectly (option 1 instead of 2) -> 1/2 = 50% >= 30%
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 1, 2, 1), 15);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
        assertEquals(taskId, response.getTaskId());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(1, response.getTotalCorrect());
        assertEquals(50.0, response.getScorePercentage());
        assertTrue(response.getPassed());
        assertEquals(DailyTaskStatus.COMPLETED, response.getStatus());

        // Verify task update
        assertEquals(DailyTaskStatus.COMPLETED, task.getStatus());
        assertEquals(25, task.getActualTimeSpent());
        assertEquals(50, task.getProgressPercent());
        verify(dailyTaskRepository).save(task);

        // Verify single DailyTaskLog update
        ArgumentCaptor<DailyTaskLog> logCaptor = ArgumentCaptor.forClass(DailyTaskLog.class);
        verify(dailyTaskLogRepository).save(logCaptor.capture());
        DailyTaskLog savedLog = logCaptor.getValue();

        assertEquals(testUser, savedLog.getUser());
        assertEquals(task, savedLog.getTask());
        assertEquals(2, savedLog.getAttempts());
        assertEquals(25, savedLog.getTotalTimeSpent());
        assertEquals(50.0, savedLog.getMaxScore());
        assertEquals(50.0, savedLog.getAiScore());
        assertEquals(DailyTaskStatus.COMPLETED, savedLog.getStatus());

        QuizPerformanceData perfData = savedLog.getPerformanceDataAs(QuizPerformanceData.class);
        assertNotNull(perfData);
        assertEquals(1, perfData.getSubmissions().size());
        assertEquals(50.0, perfData.getSubmissions().get(0).getScorePercentage());
    }

    @Test
    void submitQuiz_Failing_UpdatesTaskToPartialAndUpdatesSingleLog() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setStatus(DailyTaskStatus.PENDING);
        task.setActualTimeSpent(0);

        DailyTaskLog existingLog = new DailyTaskLog();
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);
        existingLog.setAttempts(0);
        existingLog.setTotalTimeSpent(0);
        existingLog.setMaxScore(0.0);
        existingLog.setProgressPercent(0);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        // 4 questions
        McqQuestion q1 = new McqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp");
        McqQuestion q2 = new McqQuestion(2, "Q2", List.of(opt1, opt2), opt1, "Exp");
        McqQuestion q3 = new McqQuestion(3, "Q3", List.of(opt1, opt2), opt1, "Exp");
        McqQuestion q4 = new McqQuestion(4, "Q4", List.of(opt1, opt2), opt1, "Exp");

        task.setContentJson(new QuizTaskContent(List.of(q1, q2, q3, q4)));

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // User answers 0 questions correctly -> 0% < 30%
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 2, 2, 2, 3, 2, 4, 2), 20);

        QuizSubmitResponseDto response = quizTaskService.submitQuiz(taskId, requestDto);

        assertNotNull(response);
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
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);
        existingLog.setAttempts(1);
        existingLog.setTotalTimeSpent(30);
        existingLog.setMaxScore(80.0);
        existingLog.setProgressPercent(80);

        QuizSubmitResponseDto prevSub = new QuizSubmitResponseDto();
        prevSub.setScorePercentage(80.0);
        QuizPerformanceData existingPerf = new QuizPerformanceData(List.of(prevSub));
        existingLog.setPerformanceDataJson(existingPerf);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");
        McqQuestion q1 = new McqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp");

        task.setContentJson(new QuizTaskContent(List.of(q1)));

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
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");
        McqQuestion q1 = new McqQuestion(1, "Q1", List.of(opt1, opt2), opt1, "Exp");

        task.setContentJson(new QuizTaskContent(List.of(q1)));

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
}
