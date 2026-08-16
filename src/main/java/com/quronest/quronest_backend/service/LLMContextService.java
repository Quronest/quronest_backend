package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.llm.LLMAssistantChatContextDto;
import com.quronest.quronest_backend.dto.llm.LLMTaskContextDto;
import com.quronest.quronest_backend.dto.llm.LLMUserContextDto;
import com.quronest.quronest_backend.model.AssistantChatContext;
import com.quronest.quronest_backend.model.enums.AssistantChatContextType;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.model.table.UserJourney;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LLMContextService {
    private final UserService userService;
    private final UserJourneyService userJourneyService;
    private final DailyTaskRepository dailyTaskRepository;

    public LLMContextService(UserService userService, UserJourneyService userJourneyService,
                             DailyTaskRepository dailyTaskRepository) {
        this.userService = userService;
        this.userJourneyService = userJourneyService;
        this.dailyTaskRepository = dailyTaskRepository;
    }

    public LLMUserContextDto getCurrentUserContext() {
        User user = userService.getAuthenticatedUser();
        return getUserContext(user);
    }

    public LLMUserContextDto getUserContext(UserJourney journey) {
        return new LLMUserContextDto(journey);
    }

    public LLMUserContextDto getUserContext(User user) {
        UserJourney journey = userJourneyService.getUserCurrentJourney(user);
        return getUserContext(journey);
    }

    public LLMAssistantChatContextDto getAssistantChatContext(User user,
                                                              List<AssistantChatContext> assistantChatContexts,
                                                              String userPrompt) {
        LLMUserContextDto userContextDto = getUserContext(user);
        List<LLMTaskContextDto> taskContextDtos = new ArrayList<>();

        // get task context if present
        if (assistantChatContexts != null) {
            for (AssistantChatContext chatContext : assistantChatContexts) {
                if (chatContext.getContextType() != null
                        && chatContext.getContextType().equals(AssistantChatContextType.TASK)
                        && chatContext.getTaskId() != null) {
                    DailyTask task = dailyTaskRepository.findByIdAndUser(chatContext.getTaskId(), user);
                    if (task != null) {
                        taskContextDtos.add(new LLMTaskContextDto(task));
                    }
                }
            }
        }

        return new LLMAssistantChatContextDto(userPrompt, assistantChatContexts, userContextDto, taskContextDtos);
    }
}
