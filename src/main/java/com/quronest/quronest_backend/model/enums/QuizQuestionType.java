package com.quronest.quronest_backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuizQuestionType {
    MCQ,
    SAQ;

    @JsonCreator
    public static QuizQuestionType fromValue(String value) {
        if (value == null) {
            return MCQ;
        }
        for (QuizQuestionType type : QuizQuestionType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return MCQ;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
