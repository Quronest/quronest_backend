package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.dto.llm.UserGroupSummaryGenerateDto;
import com.quronest.quronest_backend.dto.UserJourneyDto;
import com.quronest.quronest_backend.exception.JourneyAlreadyExistException;
import com.quronest.quronest_backend.exception.JourneyNotFoundException;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.model.table.UserJourney;
import com.quronest.quronest_backend.repository.UserJourneyRepository;
import com.quronest.quronest_backend.repository.UserRepository;
import com.quronest.quronest_backend.service.rabbit.JobProducerService;
import org.springframework.stereotype.Service;

@Service
public class UserJourneyService {
    private final UserService userService;
    private final LLMApiService llmApiService;
    private final UserJourneyRepository userJourneyRepository;
    private final JobProducerService jobProducerService;
    private final UserRepository userRepository;

    public UserJourneyService(UserService userService, LLMApiService llmApiService,
                              UserJourneyRepository userJourneyRepository, JobProducerService jobProducerService,
                              UserRepository userRepository) {
        this.userService = userService;
        this.llmApiService = llmApiService;
        this.userJourneyRepository = userJourneyRepository;
        this.jobProducerService = jobProducerService;
        this.userRepository = userRepository;
    }

    public JobStatusDto startUserJourney() {
        User user = userService.getAuthenticatedUser();

        UserJourney existedJourney = userJourneyRepository.findByUser(user);
        if (existedJourney != null) {
            throw new JourneyAlreadyExistException();
        }

        UserGroupSummaryGenerateDto groupSummaryGenerateDto = new UserGroupSummaryGenerateDto(user.getAcademicData(),
                                                                                              user.getPersonalData());

        jobProducerService.verifyJobAlreadyExists(user, JobType.LLM_GENERATE_USER_SUMMARY,
                                                  "Summary generation request already exists.");
        // add summary generate job
        Job job = jobProducerService.createAndSendNewJob(user, JobType.LLM_GENERATE_USER_SUMMARY,
                                                         groupSummaryGenerateDto);

        return new JobStatusDto(job);
    }

    public UserGroupSummaryDto completeUserJourneyGeneration(User user,
                                                             UserGroupSummaryGenerateDto groupSummaryGenerateDto) {
        UserJourney existedJourney = userJourneyRepository.findByUser(user);
        if (existedJourney != null) {
            throw new JourneyAlreadyExistException();
        }

        // generate new summary
        UserGroupSummaryDto userGroupSummaryDto = llmApiService.generateUserGroupSummary(groupSummaryGenerateDto);

        user.getCurrentSummary().setCurrentSummary(userGroupSummaryDto);
        UserJourney journey = new UserJourney(user, userGroupSummaryDto.getGroup(), userGroupSummaryDto.getPhase(),
                                              userGroupSummaryDto.getSummary());
        userRepository.save(user);
        userJourneyRepository.save(journey);

        return userGroupSummaryDto;
    }

    public UserJourney getUserCurrentJourney(User user) {
        UserJourney journey = userJourneyRepository.findByUserAndGroupAndPhase(user, user.getGroup(), user.getPhase());
        if (journey == null) {
            throw new JourneyNotFoundException();
        }

        return journey;
    }

    public UserJourneyDto getUserCurrentJourneyProfile() {
        User user = userService.getAuthenticatedUser();
        UserJourney journey = getUserCurrentJourney(user);

        return new UserJourneyDto(journey);
    }
}
