package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.table.AssistantChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssistantChatRoomDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("chat_summary")
    private String chatSummary;

    @JsonProperty("task_id")
    private UUID taskId;

    @JsonProperty("creation_timestamp")
    private LocalDateTime creationTimestamp;

    @JsonProperty("update_timestamp")
    private LocalDateTime updateTimestamp;

    public AssistantChatRoomDto(AssistantChatRoom room) {
        if (room != null) {
            this.id = room.getId();
            if (room.getUser() != null) {
                this.userId = room.getUser().getId();
            }
            this.title = room.getTitle();
            this.chatSummary = room.getChatSummary();
            if (room.getTask() != null) {
                this.taskId = room.getTask().getId();
            }
            this.creationTimestamp = room.getCreationTimestamp();
            this.updateTimestamp = room.getUpdateTimestamp();
        }
    }
}
