package com.quronest.quronest_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.UrlValidator;

public class CustomValidUrlValidator implements ConstraintValidator<ValidURL, String> {
    private UrlValidator urlValidator;

    @Override
    public void initialize(ValidURL constraintAnnotation) {
        String[] allowedMethods = {"http", "https"};
        this.urlValidator = new UrlValidator(
                allowedMethods,
                UrlValidator.ALLOW_LOCAL_URLS
        );
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return urlValidator.isValid(value);
    }
}
