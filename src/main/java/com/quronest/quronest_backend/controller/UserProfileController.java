package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.*;
import com.quronest.quronest_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/user")
public class UserProfileController {
    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
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

    @GetMapping("/start-journey")
    public JobCreateResponseDto evaluateUserGroupSummary() {
        return userService.startUserJourney();
    }
}
