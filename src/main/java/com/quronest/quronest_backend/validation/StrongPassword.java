package com.quronest.quronest_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default "Invalid password. A password must be at least 8 characters long, and contain at least " +
            "one digit, one lower case letter, one upper case letter, and one special character (! @ # $ % ^ & * ( ) " +
            "_ - + = { } [ ] | ; : , < > . ? / \\)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
