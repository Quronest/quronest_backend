package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.llm.LLMUserContextDto;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.model.table.UserJourney;
import org.springframework.stereotype.Service;

@Service
public class LLMContextService {
    private final UserService userService;
    private final UserJourneyService userJourneyService;

    public LLMContextService(UserService userService, UserJourneyService userJourneyService) {
        this.userService = userService;
        this.userJourneyService = userJourneyService;
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
}
