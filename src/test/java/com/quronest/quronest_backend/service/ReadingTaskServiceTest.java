package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.table.Anchor;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.AnchorRepository;
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

class ReadingTaskServiceTest {

    @Mock
    private DailyTaskRepository dailyTaskRepository;

    @Mock
    private AnchorRepository anchorRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReadingTaskService readingTaskService;

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
}
