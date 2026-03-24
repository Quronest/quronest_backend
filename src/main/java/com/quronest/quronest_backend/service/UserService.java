package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.Constants;
import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.RegisterUserDto;
import com.quronest.quronest_backend.exception.UserAlreadyAuthenticatedException;
import com.quronest.quronest_backend.exception.UserAlreadyExistException;
import com.quronest.quronest_backend.exception.UserBlackListedException;
import com.quronest.quronest_backend.model.UserAccountStatus;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.UserRepository;
import com.quronest.quronest_backend.utils.EmailNormalizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public BooleanDto registerNewUser(RegisterUserDto registerUserDto, Authentication authentication) {

        checkUserAlreadyAuthenticated(authentication);

        // check for not exists and not blacklisted
        if (isEmailOrUserNameExists(registerUserDto.getEmail(), registerUserDto.getUsername())) {
            throw new UserAlreadyExistException();
        }

        User user = getOrCreateNewUser(registerUserDto.getEmail(), registerUserDto.getUsername(),
                                       registerUserDto.getPassword(), registerUserDto.getFullname());

        // verification mail will send from here

        return new BooleanDto(true);
    }

    private User getOrCreateNewUser(String email, String username, String password, String fullname) {

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
        newUser.setAccountStatus(UserAccountStatus.INCOMPLETE);

        userRepository.save(newUser);
        return newUser;
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
        if (authentication != null && authentication.isAuthenticated()) {
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


}
