package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.AssistantChatContext;
import com.quronest.quronest_backend.model.enums.AssistantChatGenerateStatus;
import com.quronest.quronest_backend.model.enums.AssistantChatType;
import com.quronest.quronest_backend.model.table.AssistantChat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssistantChatDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("room_id")
    private UUID roomId;

    @JsonProperty("assistant_chat_room")
    @JsonAlias({"chat_room", "room", "assistant_chat_room_dto"})
    private AssistantChatRoomDto assistantChatRoomDto;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("type")
    private AssistantChatType type;

    @JsonProperty("contexts")
    private List<AssistantChatContext> contexts = new ArrayList<>();

    @JsonProperty("message")
    private String message;

    @JsonProperty("anchor_to")
    private AnchorDto anchorTo;

    @JsonProperty("generate_status")
    private AssistantChatGenerateStatus generateStatus;

    @JsonProperty("generated_at")
    private LocalDateTime generatedAt;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("creation_timestamp")
    private LocalDateTime creationTimestamp;

    @JsonProperty("update_timestamp")
    private LocalDateTime updateTimestamp;

    public AssistantChatDto(AssistantChat chat) {
        if (chat != null) {
            this.id = chat.getId();
            if (chat.getChatRoom() != null) {
                this.roomId = chat.getChatRoom().getId();
                this.assistantChatRoomDto = new AssistantChatRoomDto(chat.getChatRoom());
            }
            if (chat.getUser() != null) {
                this.userId = chat.getUser().getId();
            }
            this.type = chat.getType();
            this.contexts = chat.getContexts();
            this.message = chat.getMessage();
            if (chat.getAnchorTo() != null) {
                this.anchorTo = new AnchorDto(chat.getAnchorTo());
            }
            this.generateStatus = chat.getGenerateStatus();
            this.generatedAt = chat.getGeneratedAt();
            this.errorMessage = chat.getErrorMessage();
            this.creationTimestamp = chat.getCreationTimestamp();
            this.updateTimestamp = chat.getUpdateTimestamp();
        }
    }

    public AssistantChatDto(AssistantChat chat, AssistantChatRoomDto assistantChatRoomDto) {
        this(chat);
        this.assistantChatRoomDto = assistantChatRoomDto;
    }
}
