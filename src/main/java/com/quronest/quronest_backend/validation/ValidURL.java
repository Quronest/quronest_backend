package com.quronest.quronest_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = CustomValidUrlValidator.class)
@Documented
public @interface ValidURL {
    String message() default "Invalid email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
