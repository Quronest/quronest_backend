package com.quronest.quronest_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.EmailValidator;

public class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

    private EmailValidator emailValidator;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.emailValidator = EmailValidator.getInstance();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return emailValidator.isValid(email);
    }
}