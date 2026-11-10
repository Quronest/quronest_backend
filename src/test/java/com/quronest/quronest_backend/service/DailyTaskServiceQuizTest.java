package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.QuizConfig;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.McqQuestionOption;
import com.quronest.quronest_backend.model.QuizQuestion;
import com.quronest.quronest_backend.model.QuizTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.enums.QuizQuestionType;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.DailyTaskLog;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.AnchorRepository;
import com.quronest.quronest_backend.repository.DailyTaskLogRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import com.quronest.quronest_backend.service.rabbit.JobProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyTaskServiceQuizTest {

    @Mock
    private DailyTaskRepository dailyTaskRepository;

    @Mock
    private DailyTaskLogRepository dailyTaskLogRepository;

    @Mock
    private JobProducerService jobProducerService;

    @Mock
    private UserService userService;

    @Mock
    private LLMContextService llmContextService;

    @Mock
    private AnchorRepository anchorRepository;

    @InjectMocks
    private DailyTaskService dailyTaskService;

    private User testUser;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        taskId = UUID.randomUUID();
    }

    @Test
    void saveQuizTaskContent_AssignsPointsToQuestionsAndCalculatesTotal() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setTaskType(DailyTaskType.QUIZ);

        QuizQuestion q1 = new QuizQuestion();
        q1.setId(1);
        q1.setType(QuizQuestionType.MCQ);
        q1.setTitle("MCQ question");
        q1.setOptions(List.of(new McqQuestionOption(1, "a", "A")));

        QuizQuestion q2 = new QuizQuestion();
        q2.setId(2);
        q2.setType(QuizQuestionType.SAQ);
        q2.setTitle("SAQ question");
        q2.setSolutionText("Expected answer");

        QuizTaskContent content = new QuizTaskContent(new ArrayList<>(List.of(q1, q2)), null);

        assertNull(q1.getMaxPoints());
        assertNull(q2.getMaxPoints());
        assertNull(content.getMaxTotalPoint());

        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);

        DailyTask savedTask = dailyTaskService.saveQuizTaskContent(testUser, taskId, content);

        assertNotNull(savedTask);
        assertEquals(QuizConfig.MCQ_MAX_POINTS, q1.getMaxPoints());
        assertEquals(QuizConfig.SAQ_MAX_POINTS, q2.getMaxPoints());
        assertEquals(QuizConfig.MCQ_MAX_POINTS + QuizConfig.SAQ_MAX_POINTS, content.getMaxTotalPoint());

        QuizTaskContent savedContent = savedTask.getContentAs(QuizTaskContent.class);
        assertNotNull(savedContent);
        assertEquals(3, savedContent.getMaxTotalPoint());
        assertEquals(1, savedContent.getQuestionnaires().get(0).getMaxPoints());
        assertEquals(2, savedContent.getQuestionnaires().get(1).getMaxPoints());

        verify(dailyTaskRepository).save(task);
    }

    @Test
    void saveQuizTaskContent_TaskNotFound_ThrowsException() {
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(null);

        assertThrows(DailyTaskNotFoundException.class, () ->
                dailyTaskService.saveQuizTaskContent(testUser, taskId, new QuizTaskContent()));
    }

    @Test
    void calculateQuizTotalMaxPoints_NullQuestionnaires_ReturnsZero() {
        assertEquals(0, dailyTaskService.calculateQuizTotalMaxPoints(new QuizTaskContent(null, null)));
    }

    @Test
    void getOrCreateTaskLog_WhenExists_ReturnsExisting() {
        DailyTask task = new DailyTask();
        DailyTaskLog existingLog = new DailyTaskLog();
        when(dailyTaskLogRepository.findByTaskAndUser(task, testUser)).thenReturn(existingLog);

        DailyTaskLog result = dailyTaskService.getOrCreateTaskLog(task, testUser);

        assertSame(existingLog, result);
        verify(dailyTaskLogRepository, never()).save(any());
    }

    @Test
    void getOrCreateTaskLog_WhenNull_CreatesAndSavesNewLog() {
        DailyTask task = new DailyTask();
        DailyPlan plan = new DailyPlan();
        task.setPlan(plan);
        task.setStatus(DailyTaskStatus.PENDING);

        when(dailyTaskLogRepository.findByTaskAndUser(task, testUser)).thenReturn(null);
        when(dailyTaskLogRepository.save(any(DailyTaskLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyTaskLog result = dailyTaskService.getOrCreateTaskLog(task, testUser);

        assertNotNull(result);
        assertEquals(task, result.getTask());
        assertEquals(testUser, result.getUser());
        assertEquals(plan, result.getPlan());
        assertEquals(DailyTaskStatus.PENDING, result.getStatus());
        assertNotNull(result.getStartedAt());
        assertEquals(0, result.getAttempts());
        assertEquals(0, result.getTotalTimeSpent());
        assertEquals(0.0, result.getMaxScore());
        assertEquals(0, result.getProgressPercent());

        verify(dailyTaskLogRepository).save(any(DailyTaskLog.class));
    }
}
