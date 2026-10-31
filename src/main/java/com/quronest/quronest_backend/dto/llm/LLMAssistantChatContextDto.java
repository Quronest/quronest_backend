package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.AssistantChatContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LLMAssistantChatContextDto {
    @JsonProperty("user_prompt")
    private String userPrompt;

    @JsonProperty("chat_contexts")
    private List<AssistantChatContext> chatContexts = new ArrayList<>();

    @JsonProperty("user_context")
    private LLMUserContextDto userContext;

    @JsonProperty("task_contexts")
    private List<LLMTaskContextDto> taskContexts = new ArrayList<>();
}
