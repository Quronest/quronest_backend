package com.quronest.quronest_backend.config;

import com.quronest.quronest_backend.model.enums.EvaluationClass;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class QuizConfig {

    public static final Map<EvaluationClass, Integer> MCQ_POINTS_MAP = Map.of(
            EvaluationClass.INCORRECT, 0,
            EvaluationClass.CORRECT, 1
    );

    public static final Map<EvaluationClass, Integer> SAQ_POINTS_MAP = Map.of(
            EvaluationClass.INCORRECT, 0,
            EvaluationClass.PARTIALLY_CORRECT, 1,
            EvaluationClass.CORRECT, 2
    );

    public static final int MCQ_MAX_POINTS = 1;
    public static final int SAQ_MAX_POINTS = 2;
}
