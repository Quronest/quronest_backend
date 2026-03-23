package com.quronest.quronest_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private Pattern pattern;
    private Matcher matcher;
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()" +
            "_\\-+={}\\[\\]|;:,<>.?/\\\\])[0-9a-zA-Z!@#$%^&*()_\\-+={}\\[\\]|;:,<>.?/\\\\]{8,}$";

    @Override
    public void initialize(StrongPassword strongPassword) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return (validatePassword(password));
    }

    private boolean validatePassword(String password) {
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

}
