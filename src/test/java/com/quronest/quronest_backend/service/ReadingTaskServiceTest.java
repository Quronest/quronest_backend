package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.QuizConfig;
import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.QuizQuestionResultDto;
import com.quronest.quronest_backend.dto.QuizSubmitRequestDto;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.McqQuestionOption;
import com.quronest.quronest_backend.model.QuizPerformanceData;
import com.quronest.quronest_backend.model.QuizQuestion;
import com.quronest.quronest_backend.model.ReadingTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.EvaluationClass;
import com.quronest.quronest_backend.model.enums.QuizQuestionType;
import com.quronest.quronest_backend.model.table.Anchor;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.DailyTaskLog;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.AnchorRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReadingTaskServiceTest {

    @Mock
    private DailyTaskRepository dailyTaskRepository;

    @Mock
    private DailyTaskLogRepository dailyTaskLogRepository;

    @Mock
    private AnchorRepository anchorRepository;

    @Mock
    private UserService userService;

    @Mock
    private DailyTaskService dailyTaskService;

    @Mock
    private QuizTaskService quizTaskService;

    @Mock
    private QuronestConfig quronestConfig;

    @InjectMocks
    private ReadingTaskService readingTaskService;

    private User testUser;
    private UUID taskId;
    private DailyPlan testPlan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        taskId = UUID.randomUUID();

        testPlan = new DailyPlan();
        testPlan.setId(UUID.randomUUID());

        lenient().when(quronestConfig.getQuizPassThreshold()).thenReturn(30.0);
        lenient().when(quizTaskService.getMaxTotalPoint(any(ReadingTaskContent.class)))
                .thenAnswer(inv -> {
                    ReadingTaskContent c = inv.getArgument(0);
                    if (c == null) return 0;
                    if (c.getMaxTotalPoint() != null && c.getMaxTotalPoint() > 0) return c.getMaxTotalPoint();
                    if (c.getQuestionnaires() == null) return 0;
                    return c.getQuestionnaires().stream()
                            .mapToInt(q -> q.getMaxPoints() != null ? q.getMaxPoints() : QuizConfig.MCQ_MAX_POINTS)
                            .sum();
                });
    }

    private QuizQuestion createMcqQuestion(Integer id, String title, List<McqQuestionOption> options,
                                           McqQuestionOption solutionOption) {
        return new QuizQuestion(id, QuizQuestionType.MCQ, title, options, solutionOption, null, "Exp", QuizConfig.MCQ_MAX_POINTS);
    }

    @Test
    void getReadingTaskById_Success() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setTaskType(DailyTaskType.READING);
        task.setTitle("Test Reading Task");

        Anchor anchor = new Anchor();
        anchor.setId(UUID.randomUUID());
        anchor.setReferenceId(taskId);
        anchor.setSelectedText("Sample selected text");

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(task);
        when(anchorRepository.findByReferenceId(taskId)).thenReturn(List.of(anchor));

        DailyTaskDto result = readingTaskService.getReadingTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Test Reading Task", result.getTitle());
        assertNotNull(result.getAnchors());
        assertEquals(1, result.getAnchors().size());
        assertEquals("Sample selected text", result.getAnchors().get(0).getSelectedText());
    }

    @Test
    void getReadingTaskById_NotFound_ThrowsException() {
        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(null);

        assertThrows(DailyTaskNotFoundException.class, () -> readingTaskService.getReadingTaskById(taskId));
    }

    @Test
    void submitReadingTask_Passing_UpdatesTaskToCompletedAndCreatesOrUpdatesLog() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.READING);
        task.setStatus(DailyTaskStatus.PENDING);
        task.setActualTimeSpent(5);
        task.setProgressPercent(0);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");
        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1);
        QuizQuestion q2 = createMcqQuestion(2, "Q2", List.of(opt1, opt2), opt2);

        ReadingTaskContent content = new ReadingTaskContent();
        content.setMarkdownContent("# Reading Markdown");
        content.setQuestionnaires(List.of(q1, q2));
        task.setContentJson(content);

        DailyTaskLog existingLog = new DailyTaskLog();
        UUID logId = UUID.randomUUID();
        existingLog.setId(logId);
        existingLog.setTask(task);
        existingLog.setUser(testUser);
        existingLog.setPlan(testPlan);
        existingLog.setAttempts(1);
        existingLog.setTotalTimeSpent(5);
        existingLog.setMaxScore(0.0);
        existingLog.setProgressPercent(0);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        QuizQuestionResultDto r1 = new QuizQuestionResultDto(q1, 1, EvaluationClass.CORRECT, 1, true);
        QuizQuestionResultDto r2 = new QuizQuestionResultDto(q2, 2, EvaluationClass.CORRECT, 1, true);
        when(quizTaskService.verifyMcq(any(), eq(Map.of(1, 1, 2, 2)))).thenReturn(List.of(r1, r2));

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 1, 2, 2), 10);

        QuizSubmitResponseDto response = readingTaskService.submitReadingTask(taskId, requestDto);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(logId, response.getTaskLogId());
        assertEquals(taskId, response.getTaskId());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(2, response.getTotalCorrect());
        assertEquals(2, response.getTotalPoints());
        assertEquals(2, response.getMaxPoints());
        assertEquals(100.0, response.getScorePercentage());
        assertTrue(response.getPassed());
        assertEquals(0.0, response.getPassThreshold());
        assertEquals(10, response.getTotalTimeSpent());
        assertEquals(100, response.getCalculatedProgress());
        assertEquals(2, response.getQuestions().size());

        // Verify DailyTask update
        ArgumentCaptor<DailyTask> taskCaptor = ArgumentCaptor.forClass(DailyTask.class);
        verify(dailyTaskRepository).save(taskCaptor.capture());
        DailyTask savedTask = taskCaptor.getValue();
        assertEquals(DailyTaskStatus.COMPLETED, savedTask.getStatus());
        assertEquals(15, savedTask.getActualTimeSpent()); // 5 + 10
        assertEquals(100, savedTask.getProgressPercent());

        // Verify DailyTaskLog update
        ArgumentCaptor<DailyTaskLog> logCaptor = ArgumentCaptor.forClass(DailyTaskLog.class);
        verify(dailyTaskLogRepository).save(logCaptor.capture());
        DailyTaskLog savedLog = logCaptor.getValue();
        assertEquals(2, savedLog.getAttempts());
        assertEquals(15, savedLog.getTotalTimeSpent());
        assertEquals(100.0, savedLog.getMaxScore());
        assertEquals(100.0, savedLog.getAiScore());
        assertEquals(100, savedLog.getProgressPercent());
        assertEquals(DailyTaskStatus.COMPLETED, savedLog.getStatus());
        assertNotNull(savedLog.getCompletedAt());
        assertNotNull(savedLog.getLastAttemptAt());

        QuizPerformanceData perfData = savedLog.getPerformanceDataAs(QuizPerformanceData.class);
        assertNotNull(perfData);
        assertNotNull(perfData.getId());
        assertEquals(2, perfData.getTotalPoints());
        assertEquals(2, perfData.getMaxPoints());
        assertEquals(1, perfData.getSubmissions().size());
        assertEquals(response.getId(), perfData.getSubmissions().get(0).getId());
    }

    @Test
    void submitReadingTask_Failing_UpdatesTaskToPartial() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.READING);
        task.setStatus(DailyTaskStatus.PENDING);
        task.setActualTimeSpent(0);
        task.setProgressPercent(0);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");
        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1);
        QuizQuestion q2 = createMcqQuestion(2, "Q2", List.of(opt1, opt2), opt2);
        QuizQuestion q3 = createMcqQuestion(3, "Q3", List.of(opt1, opt2), opt1);
        QuizQuestion q4 = createMcqQuestion(4, "Q4", List.of(opt1, opt2), opt2);

        ReadingTaskContent content = new ReadingTaskContent();
        content.setQuestionnaires(List.of(q1, q2, q3, q4));
        task.setContentJson(content);

        DailyTaskLog existingLog = new DailyTaskLog(task, testUser);
        existingLog.setId(UUID.randomUUID());

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // 0 correct out of 4 -> score is 0 -> failing
        QuizQuestionResultDto r1 = new QuizQuestionResultDto(q1, 2, EvaluationClass.INCORRECT, 0, false);
        QuizQuestionResultDto r2 = new QuizQuestionResultDto(q2, 1, EvaluationClass.INCORRECT, 0, false);
        QuizQuestionResultDto r3 = new QuizQuestionResultDto(q3, 2, EvaluationClass.INCORRECT, 0, false);
        QuizQuestionResultDto r4 = new QuizQuestionResultDto(q4, 1, EvaluationClass.INCORRECT, 0, false);
        when(quizTaskService.verifyMcq(any(), any())).thenReturn(List.of(r1, r2, r3, r4));

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 2), 8);

        QuizSubmitResponseDto response = readingTaskService.submitReadingTask(taskId, requestDto);

        assertNotNull(response);
        assertFalse(response.getPassed());
        assertEquals(0.0, response.getScorePercentage());
        assertEquals(0, response.getCalculatedProgress());

        assertEquals(DailyTaskStatus.PARTIAL, task.getStatus());
        assertEquals(0, task.getProgressPercent());
        assertNull(existingLog.getCompletedAt());
    }

    @Test
    void submitReadingTask_ScoreNotZero_PassesAndSetsProgressTo100() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setPlan(testPlan);
        task.setTaskType(DailyTaskType.READING);
        task.setStatus(DailyTaskStatus.PENDING);
        task.setActualTimeSpent(0);
        task.setProgressPercent(0);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");
        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1, opt2), opt1);
        QuizQuestion q2 = createMcqQuestion(2, "Q2", List.of(opt1, opt2), opt2);
        QuizQuestion q3 = createMcqQuestion(3, "Q3", List.of(opt1, opt2), opt1);
        QuizQuestion q4 = createMcqQuestion(4, "Q4", List.of(opt1, opt2), opt2);

        ReadingTaskContent content = new ReadingTaskContent();
        content.setQuestionnaires(List.of(q1, q2, q3, q4));
        task.setContentJson(content);

        DailyTaskLog existingLog = new DailyTaskLog(task, testUser);
        existingLog.setId(UUID.randomUUID());

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        // 1 correct out of 4 -> score is 1 (not 0) -> PASSES!
        QuizQuestionResultDto r1 = new QuizQuestionResultDto(q1, 1, EvaluationClass.CORRECT, 1, true);
        QuizQuestionResultDto r2 = new QuizQuestionResultDto(q2, 1, EvaluationClass.INCORRECT, 0, false);
        QuizQuestionResultDto r3 = new QuizQuestionResultDto(q3, 2, EvaluationClass.INCORRECT, 0, false);
        QuizQuestionResultDto r4 = new QuizQuestionResultDto(q4, 1, EvaluationClass.INCORRECT, 0, false);
        when(quizTaskService.verifyMcq(any(), any())).thenReturn(List.of(r1, r2, r3, r4));

        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 1), 8);

        QuizSubmitResponseDto response = readingTaskService.submitReadingTask(taskId, requestDto);

        assertNotNull(response);
        assertTrue(response.getPassed());
        assertEquals(25.0, response.getScorePercentage());
        assertEquals(100, response.getCalculatedProgress());

        assertEquals(DailyTaskStatus.COMPLETED, task.getStatus());
        assertEquals(100, task.getProgressPercent());
        assertNotNull(existingLog.getCompletedAt());
    }

    @Test
    void submitReadingTask_NotFound_ThrowsException() {
        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(null);

        assertThrows(DailyTaskNotFoundException.class, () ->
                readingTaskService.submitReadingTask(taskId, new QuizSubmitRequestDto()));
    }

    @Test
    void submitReadingTask_EmptyQuestionnaires_HandlesGracefully() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setTaskType(DailyTaskType.READING);
        task.setStatus(DailyTaskStatus.PENDING);

        ReadingTaskContent content = new ReadingTaskContent();
        content.setQuestionnaires(List.of());
        task.setContentJson(content);

        DailyTaskLog existingLog = new DailyTaskLog(task, testUser);
        existingLog.setId(UUID.randomUUID());

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);
        when(quizTaskService.verifyMcq(any(), any())).thenReturn(List.of());

        QuizSubmitResponseDto response = readingTaskService.submitReadingTask(taskId, null);

        assertNotNull(response);
        assertEquals(0, response.getTotalQuestions());
        assertEquals(0, response.getTotalPoints());
        assertEquals(0, response.getMaxPoints());
        assertEquals(0.0, response.getScorePercentage());
        assertFalse(response.getPassed());
    }

    @Test
    void submitReadingTask_WithStoredMaxTotalPoint_UsesStoredValue() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setTaskType(DailyTaskType.READING);
        task.setStatus(DailyTaskStatus.PENDING);

        ReadingTaskContent content = new ReadingTaskContent();
        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "A");
        QuizQuestion q1 = createMcqQuestion(1, "Q1", List.of(opt1), opt1);
        content.setQuestionnaires(List.of(q1));
        content.setMaxTotalPoint(10); // explicitly stored 10
        task.setContentJson(content);

        DailyTaskLog existingLog = new DailyTaskLog(task, testUser);
        existingLog.setId(UUID.randomUUID());

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.READING)).thenReturn(task);
        when(dailyTaskService.getOrCreateTaskLog(task, testUser)).thenReturn(existingLog);

        QuizQuestionResultDto r1 = new QuizQuestionResultDto(q1, 1, EvaluationClass.CORRECT, 1, true);
        when(quizTaskService.verifyMcq(any(), any())).thenReturn(List.of(r1));

        QuizSubmitResponseDto response = readingTaskService.submitReadingTask(taskId, new QuizSubmitRequestDto(Map.of(1, 1), 5));

        assertNotNull(response);
        assertEquals(10, response.getMaxPoints());
        assertEquals(10.0, response.getScorePercentage()); // 1 / 10 * 100
        assertTrue(response.getPassed()); // 1 > 0
    }
}
