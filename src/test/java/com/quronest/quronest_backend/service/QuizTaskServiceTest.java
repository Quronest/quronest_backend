package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.QuizQuestionDto;
import com.quronest.quronest_backend.dto.QuizTaskContentDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.McqQuestion;
import com.quronest.quronest_backend.model.McqQuestionOption;
import com.quronest.quronest_backend.model.QuizTaskContent;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.DailyTaskLogRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizTaskServiceTest {

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        taskId = UUID.randomUUID();
    }

    @Test
    void getQuizTaskById_Success() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setTaskType(DailyTaskType.QUIZ);

        McqQuestionOption opt1 = new McqQuestionOption(1, "a", "Option A");
        McqQuestionOption opt2 = new McqQuestionOption(2, "b", "Option B");

        McqQuestion question = new McqQuestion(
                1,
                "Sample Question?",
                List.of(opt1, opt2),
                opt1,
                "Explanation here"
        );

        QuizTaskContent content = new QuizTaskContent(List.of(question));
        task.setContentJson(content);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);

        DailyTaskDto result = quizTaskService.getQuizTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertTrue(result.getContent() instanceof QuizTaskContentDto);

        QuizTaskContentDto contentDto = (QuizTaskContentDto) result.getContent();
        assertEquals(1, contentDto.getQuestionnaires().size());

        QuizQuestionDto questionDto = contentDto.getQuestionnaires().get(0);
        assertEquals(1, questionDto.getId());
        assertEquals("Sample Question?", questionDto.getTitle());
        assertEquals(2, questionDto.getOptions().size());
    }

    @Test
    void getQuizTaskById_NullContent() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setContent(null);

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);

        DailyTaskDto result = quizTaskService.getQuizTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertNull(result.getContent());
    }

    @Test
    void getQuizTaskById_EmptyQuestionnaires() {
        DailyTask task = new DailyTask();
        task.setId(taskId);
        task.setUser(testUser);
        task.setTaskType(DailyTaskType.QUIZ);
        task.setContentJson(new QuizTaskContent(List.of()));

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(task);

        DailyTaskDto result = quizTaskService.getQuizTaskById(taskId);

        assertNotNull(result);
        assertTrue(result.getContent() instanceof QuizTaskContentDto);
        QuizTaskContentDto contentDto = (QuizTaskContentDto) result.getContent();
        assertTrue(contentDto.getQuestionnaires().isEmpty());
    }

    @Test
    void getQuizTaskById_NotFound_ThrowsException() {
        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(dailyTaskRepository.findByIdAndUserAndTaskType(taskId, testUser, DailyTaskType.QUIZ)).thenReturn(null);

        assertThrows(DailyTaskNotFoundException.class, () -> quizTaskService.getQuizTaskById(taskId));
    }
}
