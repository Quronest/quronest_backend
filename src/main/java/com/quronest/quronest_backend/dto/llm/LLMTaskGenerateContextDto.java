package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LLMTaskGenerateContextDto {
    @JsonProperty("task_id")
    private UUID taskId;

    @JsonProperty("task_context")
    private LLMTaskContextDto taskContext;

    @JsonProperty("user_context")
    private LLMUserContextDto usesContext;
}
