package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.AssistantChatContext;
import com.quronest.quronest_backend.model.enums.AssistantChatGenerateStatus;
import com.quronest.quronest_backend.model.enums.AssistantChatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assistant_chat")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssistantChat {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private AssistantChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AssistantChatType type = AssistantChatType.ASSISTANT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contexts", columnDefinition = "jsonb")
    private List<AssistantChatContext> contexts = new ArrayList<>();

    @Column(name = "user_chat_id")
    private UUID userChatId;

    @Column(name = "message")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anchor_to")
    private Anchor anchorTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "generate_status")
    private AssistantChatGenerateStatus generateStatus;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public AssistantChat(AssistantChatRoom chatRoom, User user, AssistantChatType type,
                         List<AssistantChatContext> contexts, String message) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.type = type;
        this.contexts = contexts;
        this.message = message;
    }

    public AssistantChat(AssistantChatRoom chatRoom, User user, AssistantChatType type,
                         UUID userChatId, AssistantChatGenerateStatus generateStatus) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.type = type;
        this.userChatId = userChatId;
        this.generateStatus = generateStatus;
    }
}
