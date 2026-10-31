package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.AssistantChatContext;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssistantAskDto {
    @JsonProperty("room_id")
    private UUID roomId;

    @JsonProperty("contexts")
    private List<AssistantChatContext> contexts;

    @JsonProperty("message")
    @Size(max = 50000, message = "Message length must be within 50000 character")
    private String message;
}
