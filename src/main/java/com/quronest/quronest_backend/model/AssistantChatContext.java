package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.AssistantChatContextType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AssistantChatContext {
    @JsonProperty("context_type")
    private AssistantChatContextType contextType;

    @JsonProperty("context_text")
    private String contextText;

    @JsonProperty("task_id")
    private UUID taskId;
}
