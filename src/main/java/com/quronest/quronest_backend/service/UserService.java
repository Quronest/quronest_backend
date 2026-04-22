package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.Constants;
import com.quronest.quronest_backend.dto.*;
import com.quronest.quronest_backend.exception.*;
import com.quronest.quronest_backend.model.UserAcademicData;
import com.quronest.quronest_backend.model.UserAccountStatus;
import com.quronest.quronest_backend.model.UserPersonalData;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.UserRepository;
import com.quronest.quronest_backend.utils.EmailNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LLMApiService llmApiService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, LLMApiService llmApiService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.llmApiService = llmApiService;
    }

    public BooleanDto registerNewUser(RegisterUserDto registerUserDto, Authentication authentication) {

        checkUserAlreadyAuthenticated(authentication);

        // check for not exists and not blacklisted
        if (isEmailOrUserNameExists(registerUserDto.getEmail(), registerUserDto.getUsername())) {
            throw new UserAlreadyExistException();
        }

        User user = getOrCreateNewUser(registerUserDto.getEmail(), registerUserDto.getUsername(),
                                       registerUserDto.getPassword(), registerUserDto.getFullname(), null);

        // verification mail will send from here

        return new BooleanDto(true);
    }

    private User getOrCreateNewUser(String email, String username, String password, String fullname, String avatarUrl) {

        // Check if user already exists which is not blacklisted
        User user = getUserByEmailNotBlacklisted(email);
        if (user != null) {
            return user;
        }

        // save normalized email
        String normalizedEmail = EmailNormalizer.normalize(email);

        User newUser = new User(fullname, normalizedEmail);
        newUser.setRoles(Constants.USER_ROLE_DEFAULT);

        // encode password and save
        if (password != null && !password.isBlank()) {
            newUser.setPassword(passwordEncoder.encode(password));
        } else {
            newUser.setPassword(null);
        }

        // set or create username;
        String uniqueUsername = username;
        if (uniqueUsername == null) {
            uniqueUsername = email.split("@")[0].toLowerCase();
            if (userRepository.existsByUsername(uniqueUsername)) {
                uniqueUsername = generateRandomUsername(uniqueUsername);
            }
        }
        newUser.setUsername(uniqueUsername);
        newUser.setAvatar(avatarUrl);
        newUser.setAccountStatus(UserAccountStatus.INCOMPLETE);

        userRepository.save(newUser);
        return newUser;
    }

    public UserProfileDto getAuthenticatedUserProfile() {
        User user = getAuthenticatedUser();
        return new UserProfileDto(user);
    }

    public boolean isEmailOrUserNameExists(String email, String username) {
        User user = userRepository.findByEmailOrUsername(email, username);
        if (user == null) {
            return false;
        }

        if (user.isBlacklisted()) {
            throw new UserBlackListedException();
        }

        return true;
    }

    public void checkUserAlreadyAuthenticated(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            throw new UserAlreadyAuthenticatedException();
        }
    }

    public User getUserByEmailNotBlacklisted(String email) {
        User user = userRepository.findByEmailAndBlacklistedFalse(email);
        if (user != null) {
            return user;
        }

        String normalizedEmail = EmailNormalizer.normalize(email);
        user = userRepository.findByEmailAndBlacklistedFalse(normalizedEmail);
        return user;
    }

    private String generateRandomUsername(String prefix) {
        for (int i = 0; i < 5; i++) {
            String suffix = UUID.randomUUID().toString().substring(0, 4);
            String username = prefix + "_" + suffix;

            if (!userRepository.existsByUsername(username)) {
                return username;
            }
        }

        // fallback (very rare)
        return prefix + "_" + System.currentTimeMillis() % 10000;
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {

            throw new UserNotAuthenticatedException();
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }

        User dbUser = userRepository.findByEmail(email);

        if (dbUser == null) {
            throw new UserNotFoundException();
        }

        return dbUser;
    }

    public User getOrCreateOAuth2User(String email, String name, String avatarUrl) {
        User user = getOrCreateNewUser(email, null, null, name, avatarUrl);
        setEmailVerifiedAndFinishRegistration(user);
        return user;
    }

    public User setEmailVerifiedAndFinishRegistration(User user) {
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }
        return user;
    }

    public BooleanDto addUserAcademicData(UserAcademicDataDto userAcademicDataDto) {
        User user = getAuthenticatedUser();

        UserAcademicData academicData = new UserAcademicData(userAcademicDataDto);
        user.setAcademicData(academicData);

        userRepository.save(user);
        return new BooleanDto(true);
    }

    public BooleanDto addUserPersonalData(UserPersonalDataDto userPersonalDataDto) {
        User user = getAuthenticatedUser();

        UserPersonalData personalData = new UserPersonalData(userPersonalDataDto);
        user.setPersonalData(personalData);

        userRepository.save(user);
        return new BooleanDto(true);
    }

    public UserGroupSummaryDto evaluateUserGroupSummary() {
        User user = getAuthenticatedUser();

        UserGroupSummaryGenerateDto groupSummaryGenerateDto = new UserGroupSummaryGenerateDto(user.getAcademicData(),
                                                                                              user.getPersonalData());
        UserGroupSummaryDto userGroupSummaryDto = llmApiService.generateUserGroupSummary(groupSummaryGenerateDto);
        user.getInternalData().setUserGroupSummary(userGroupSummaryDto);

        userRepository.save(user);
        return userGroupSummaryDto;
    }
}
