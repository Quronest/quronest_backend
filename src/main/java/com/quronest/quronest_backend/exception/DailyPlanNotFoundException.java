package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class DailyPlanNotFoundException extends CustomApiErrorResponseException {
    public DailyPlanNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "daily_plan_not_found", message);
    }

    public DailyPlanNotFoundException() {
        this("Daily plan not found.");
    }
}
