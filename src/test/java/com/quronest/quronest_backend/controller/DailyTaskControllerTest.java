package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.dto.QuizSubmitRequestDto;
import com.quronest.quronest_backend.dto.QuizSubmitResponseDto;
import com.quronest.quronest_backend.service.DailyTaskService;
import com.quronest.quronest_backend.service.QuizTaskService;
import com.quronest.quronest_backend.service.ReadingTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyTaskControllerTest {

    @Mock
    private DailyTaskService dailyTaskService;

    @Mock
    private QuizTaskService quizTaskService;

    @Mock
    private ReadingTaskService readingTaskService;

    @InjectMocks
    private DailyTaskController dailyTaskController;

    private UUID taskId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
    }

    @Test
    void getReadingTaskById_DelegatesToReadingTaskService() {
        DailyTaskDto expected = new DailyTaskDto();
        expected.setId(taskId);
        when(readingTaskService.getReadingTaskById(taskId)).thenReturn(expected);

        DailyTaskDto result = dailyTaskController.getReadingTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        verify(readingTaskService).getReadingTaskById(taskId);
    }

    @Test
    void submitReadingTask_DelegatesToReadingTaskService() {
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 1), 10);
        QuizSubmitResponseDto expected = new QuizSubmitResponseDto();
        expected.setTaskId(taskId);
        when(readingTaskService.submitReadingTask(taskId, requestDto)).thenReturn(expected);

        QuizSubmitResponseDto result = dailyTaskController.submitReadingTask(taskId, requestDto);

        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        verify(readingTaskService).submitReadingTask(taskId, requestDto);
    }

    @Test
    void getQuizTaskById_DelegatesToQuizTaskService() {
        DailyTaskDto expected = new DailyTaskDto();
        expected.setId(taskId);
        when(quizTaskService.getQuizTaskById(taskId)).thenReturn(expected);

        DailyTaskDto result = dailyTaskController.getQuizTaskById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        verify(quizTaskService).getQuizTaskById(taskId);
    }

    @Test
    void submitQuiz_DelegatesToQuizTaskService() {
        QuizSubmitRequestDto requestDto = new QuizSubmitRequestDto(Map.of(1, 1), 10);
        QuizSubmitResponseDto expected = new QuizSubmitResponseDto();
        expected.setTaskId(taskId);
        when(quizTaskService.submitQuiz(taskId, requestDto)).thenReturn(expected);

        QuizSubmitResponseDto result = dailyTaskController.submitQuiz(taskId, requestDto);

        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        verify(quizTaskService).submitQuiz(taskId, requestDto);
    }
}
