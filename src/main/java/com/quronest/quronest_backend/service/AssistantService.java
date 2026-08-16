package com.quronest.quronest_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.dto.AssistantAskDto;
import com.quronest.quronest_backend.dto.AssistantChatDto;
import com.quronest.quronest_backend.dto.AssistantStreamDataDto;
import com.quronest.quronest_backend.dto.llm.LLMAssistantChatContextDto;
import com.quronest.quronest_backend.exception.AssistantChatRoomNotFoundException;
import com.quronest.quronest_backend.model.enums.AssistantChatGenerateStatus;
import com.quronest.quronest_backend.model.enums.AssistantChatType;
import com.quronest.quronest_backend.model.enums.AssistantStreamFlag;
import com.quronest.quronest_backend.model.table.AssistantChat;
import com.quronest.quronest_backend.model.table.AssistantChatRoom;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.AssistantChatRepository;
import com.quronest.quronest_backend.repository.AssistantChatRoomRepository;
import com.quronest.quronest_backend.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AssistantService {
    private static final Log log = LogFactory.getLog(AssistantService.class);

    private final AssistantChatRepository assistantChatRepository;
    private final UserService userService;
    private final AssistantChatRoomRepository assistantChatRoomRepository;
    private final LLMApiService llmApiService;
    private final LLMContextService llmContextService;

    public AssistantService(AssistantChatRepository assistantChatRepository, UserService userService,
                            AssistantChatRoomRepository assistantChatRoomRepository, LLMApiService llmApiService,
                            LLMContextService llmContextService) {
        this.assistantChatRepository = assistantChatRepository;
        this.userService = userService;
        this.assistantChatRoomRepository = assistantChatRoomRepository;
        this.llmApiService = llmApiService;
        this.llmContextService = llmContextService;
    }

    @Transactional
    public Flux<String> askAssistant(AssistantAskDto askDto) {
        User user = userService.getAuthenticatedUser();
        AssistantChatRoom chatRoom = getOrCreateChatRoom(askDto.getRoomId(), user);

        // create user chat
        AssistantChat userChat = new AssistantChat(chatRoom, user, AssistantChatType.USER, askDto.getContexts(),
                                                   askDto.getMessage());
        assistantChatRepository.save(userChat);

        // call stream api
        LLMAssistantChatContextDto assistantChatContextDto = llmContextService.getAssistantChatContext(user,
                                                                                                       askDto.getContexts(),
                                                                                                       askDto.getMessage());

        AssistantStreamSession session = new AssistantStreamSession(chatRoom, user, userChat);

        return llmApiService.getAssistantChatStream(assistantChatContextDto)
                .doOnNext(rawChunk -> handleStreamChunk(session, rawChunk))
                .doOnError(error -> handleStreamException(session, error));
    }

    private AssistantChatRoom getOrCreateChatRoom(UUID roomId, User user) {
        if (roomId == null) {
            AssistantChatRoom newRoom = new AssistantChatRoom(user);
            assistantChatRoomRepository.save(newRoom);

            return newRoom;
        }

        AssistantChatRoom chatRoom = assistantChatRoomRepository.findByIdAndUser(roomId, user);
        if (chatRoom == null) {
            throw new AssistantChatRoomNotFoundException();
        }

        return chatRoom;
    }

    public Page<AssistantChatDto> getAssistantChats(UUID roomId, Pageable pageable) {
        User user = userService.getAuthenticatedUser();
        AssistantChatRoom chatRoom = assistantChatRoomRepository.findByIdAndUser(roomId, user);
        if (chatRoom == null) {
            throw new AssistantChatRoomNotFoundException();
        }

        Page<AssistantChat> chats = assistantChatRepository.findByChatRoomAndUser(chatRoom, user, pageable);
        return chats.map(AssistantChatDto::new);
    }

    public AssistantStreamDataDto parseStreamChunk(String rawChunk) {
        if (rawChunk == null || rawChunk.isBlank()) {
            return null;
        }

        try {
            String json = rawChunk.trim();
            if (json.startsWith("data:")) {
                json = json.substring(5).trim();
            }

            if (json.isEmpty() || "[DONE]".equalsIgnoreCase(json)) {
                return null;
            }

            JsonNode rootNode = JsonUtils.readTree(json);
            if (rootNode == null || !rootNode.has("flag")) {
                return null;
            }

            String flagStr = rootNode.get("flag").asText();
            AssistantStreamFlag flag = AssistantStreamFlag.valueOf(flagStr.toUpperCase());

            String data = null;
            if (rootNode.has("data") && !rootNode.get("data").isNull()) {
                JsonNode dataNode = rootNode.get("data");
                data = dataNode.isTextual() ? dataNode.asText() : dataNode.toString();
            }

            return new AssistantStreamDataDto(flag, data);
        } catch (Exception e) {
            log.error("Failed to parse assistant stream chunk: " + rawChunk, e);
            return null;
        }
    }

    public void handleStreamChunk(AssistantStreamSession session, String rawChunk) {
        AssistantStreamDataDto streamData = parseStreamChunk(rawChunk);
        if (streamData == null || streamData.getFlag() == null) {
            return;
        }

        switch (streamData.getFlag()) {
            case START -> handleStartEvent(session, streamData);
            case CHUNK -> handleChunkEvent(session, streamData);
            case METADATA -> handleMetadataEvent(session, streamData);
            case ERROR -> handleErrorEvent(session, streamData);
            case DONE -> handleDoneEvent(session, streamData);
        }
    }

    private void handleStartEvent(AssistantStreamSession session, AssistantStreamDataDto streamData) {
        try {
            AssistantChat assistantChat = new AssistantChat(
                    session.getChatRoom(),
                    session.getUser(),
                    AssistantChatType.ASSISTANT,
                    session.getUserChat().getId(),
                    AssistantChatGenerateStatus.RUNNING
            );

            assistantChat = assistantChatRepository.save(assistantChat);
            session.setAssistantChat(assistantChat);
        } catch (Exception e) {
            log.error("Failed to handle START event for assistant chat", e);
        }
    }

    private void handleChunkEvent(AssistantStreamSession session, AssistantStreamDataDto streamData) {
        if (streamData.getData() == null || streamData.getData().isBlank()) {
            return;
        }

        try {
            String text = null;
            JsonNode dataNode = JsonUtils.readTree(streamData.getData());
            if (dataNode != null && dataNode.has("content")) {
                text = dataNode.get("content").asText();
            } else {
                text = streamData.getData();
            }

            if (text != null && !text.isEmpty()) {
                session.getContentBuilder().append(text);
            }
        } catch (Exception e) {
            session.getContentBuilder().append(streamData.getData());
        }
    }

    private void handleMetadataEvent(AssistantStreamSession session, AssistantStreamDataDto streamData) {
        if (streamData.getData() == null || streamData.getData().isBlank()) {
            return;
        }

        try {
            JsonNode metadataNode = JsonUtils.readTree(streamData.getData());
            if (metadataNode == null) {
                return;
            }

            String title = metadataNode.has("title") && !metadataNode.get("title").isNull()
                    ? metadataNode.get("title").asText()
                    : null;
            String summary = metadataNode.has("summary") && !metadataNode.get("summary").isNull()
                    ? metadataNode.get("summary").asText()
                    : null;

            AssistantChatRoom chatRoom = session.getChatRoom();

            // title is updated only if there is no title in the room
            if (title != null && !title.isBlank() && (chatRoom.getTitle() == null || chatRoom.getTitle().isBlank())) {
                chatRoom.setTitle(title);
            }

            // summary is updated every time
            if (summary != null && !summary.isBlank()) {
                chatRoom.setChatSummary(summary);
            }

            assistantChatRoomRepository.save(chatRoom);
        } catch (Exception e) {
            log.error("Failed to handle METADATA event for assistant chat room", e);
        }
    }

    private void handleErrorEvent(AssistantStreamSession session, AssistantStreamDataDto streamData) {
        try {
            String errorMessage = "Stream error occurred";
            if (streamData.getData() != null && !streamData.getData().isBlank()) {
                try {
                    JsonNode errorNode = JsonUtils.readTree(streamData.getData());
                    if (errorNode != null && errorNode.has("error")) {
                        errorMessage = errorNode.get("error").asText();
                    } else {
                        errorMessage = streamData.getData();
                    }
                } catch (Exception e) {
                    errorMessage = streamData.getData();
                }
            }

            AssistantChat assistantChat = session.getAssistantChat();
            if (assistantChat != null) {
                assistantChat.setErrorMessage(errorMessage);
                assistantChat.setGenerateStatus(AssistantChatGenerateStatus.FAILED);
                assistantChatRepository.save(assistantChat);
            }
        } catch (Exception e) {
            log.error("Failed to handle ERROR event for assistant chat", e);
        }
    }

    private void handleDoneEvent(AssistantStreamSession session, AssistantStreamDataDto streamData) {
        try {
            AssistantChat assistantChat = session.getAssistantChat();
            if (assistantChat != null && assistantChat.getGenerateStatus() != AssistantChatGenerateStatus.FAILED) {
                assistantChat.setMessage(session.getContentBuilder().toString());
                assistantChat.setGenerateStatus(AssistantChatGenerateStatus.COMPLETED);
                assistantChat.setGeneratedAt(LocalDateTime.now());
                assistantChatRepository.save(assistantChat);
            }
        } catch (Exception e) {
            log.error("Failed to handle DONE event for assistant chat", e);
        }
    }

    private void handleStreamException(AssistantStreamSession session, Throwable error) {
        log.error("Exception occurred during assistant chat stream", error);
        try {
            AssistantChat assistantChat = session.getAssistantChat();
            if (assistantChat != null && assistantChat.getGenerateStatus() == AssistantChatGenerateStatus.RUNNING) {
                assistantChat.setErrorMessage(error != null ? error.getMessage() : "Unknown stream error");
                assistantChat.setGenerateStatus(AssistantChatGenerateStatus.FAILED);
                assistantChatRepository.save(assistantChat);
            }
        } catch (Exception e) {
            log.error("Failed to update assistant chat status on stream exception", e);
        }
    }

    @Getter
    @Setter
    public static class AssistantStreamSession {
        private final AssistantChatRoom chatRoom;
        private final User user;
        private final AssistantChat userChat;
        private final StringBuilder contentBuilder = new StringBuilder();
        private AssistantChat assistantChat;

        public AssistantStreamSession(AssistantChatRoom chatRoom, User user, AssistantChat userChat) {
            this.chatRoom = chatRoom;
            this.user = user;
            this.userChat = userChat;
        }

    }
}
