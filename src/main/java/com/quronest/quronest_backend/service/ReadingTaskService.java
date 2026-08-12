package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.AnchorDto;
import com.quronest.quronest_backend.dto.DailyTaskDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.AnchorRepository;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReadingTaskService {

    private final DailyTaskRepository dailyTaskRepository;
    private final AnchorRepository anchorRepository;
    private final UserService userService;

    public ReadingTaskService(DailyTaskRepository dailyTaskRepository,
                              AnchorRepository anchorRepository,
                              UserService userService) {
        this.dailyTaskRepository = dailyTaskRepository;
        this.anchorRepository = anchorRepository;
        this.userService = userService;
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
}
