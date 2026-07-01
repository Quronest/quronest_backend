package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.*;
import com.quronest.quronest_backend.service.UserJourneyService;
import com.quronest.quronest_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/user")
public class UserProfileController {
    private final UserService userService;
    private final UserJourneyService userJourneyService;

    public UserProfileController(UserService userService, UserJourneyService userJourneyService) {
        this.userService = userService;
        this.userJourneyService = userJourneyService;
    }

    @GetMapping("/profile")
    public UserProfileDto getUserProfile() {
        return userService.getAuthenticatedUserProfile();
    }

    @PostMapping("/academic-data")
    public BooleanDto addUserAcademicData(@RequestBody UserAcademicDataDto userAcademicDataDto) {
        return userService.addUserAcademicData(userAcademicDataDto);
    }

    @PostMapping("/personal-data")
    public BooleanDto addUserPersonalData(@RequestBody UserPersonalDataDto userPersonalDataDto) {
        return userService.addUserPersonalData(userPersonalDataDto);
    }

    @PostMapping("/start-journey")
    public JobCreateResponseDto evaluateUserGroupSummary() {
        return userJourneyService.startUserJourney();
    }

    @GetMapping("/group-summary")
    public UserGroupSummaryDto getUserGroupSummary() {
        return userService.getUserCurrentSummary();
    }

    @GetMapping("/current-journey")
    public UserJourneyDto getUserCurrentJourney() {
        return userJourneyService.getUserCurrentJourneyProfile();
    }

}
