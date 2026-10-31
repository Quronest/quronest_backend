package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.AssistantAskDto;
import com.quronest.quronest_backend.dto.AssistantChatDto;
import com.quronest.quronest_backend.dto.AssistantStreamDataDto;
import com.quronest.quronest_backend.dto.llm.LLMAssistantChatContextDto;
import com.quronest.quronest_backend.exception.AssistantChatRoomNotFoundException;
import com.quronest.quronest_backend.model.AssistantChatContext;
import com.quronest.quronest_backend.model.enums.AssistantChatGenerateStatus;
import com.quronest.quronest_backend.model.enums.AssistantChatType;
import com.quronest.quronest_backend.model.enums.AssistantStreamFlag;
import com.quronest.quronest_backend.model.table.AssistantChat;
import com.quronest.quronest_backend.model.table.AssistantChatRoom;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.AssistantChatRepository;
import com.quronest.quronest_backend.repository.AssistantChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AssistantServiceTest {

    private AssistantService assistantService;
    private AssistantChatRepository assistantChatRepository;
    private AssistantChatRoomRepository assistantChatRoomRepository;
    private UserService userService;
    private LLMApiService llmApiService;
    private LLMContextService llmContextService;

    @BeforeEach
    void setUp() {
        assistantChatRepository = Mockito.mock(AssistantChatRepository.class);
        userService = Mockito.mock(UserService.class);
        assistantChatRoomRepository = Mockito.mock(AssistantChatRoomRepository.class);
        llmApiService = Mockito.mock(LLMApiService.class);
        llmContextService = Mockito.mock(LLMContextService.class);

        when(assistantChatRepository.save(any(AssistantChat.class))).thenAnswer(invocation -> {
            AssistantChat chat = invocation.getArgument(0);
            if (chat.getId() == null) {
                chat.setId(UUID.randomUUID());
            }
            return chat;
        });

        when(assistantChatRoomRepository.save(any(AssistantChatRoom.class))).thenAnswer(invocation -> {
            AssistantChatRoom room = invocation.getArgument(0);
            if (room.getId() == null) {
                room.setId(UUID.randomUUID());
            }
            return room;
        });

        assistantService = new AssistantService(
                assistantChatRepository,
                userService,
                assistantChatRoomRepository,
                llmApiService,
                llmContextService
        );
    }

    @Test
    void testParseStartChunk() {
        String raw = "data: {\"flag\":\"START\",\"data\":{\"timestamp\":\"2026-08-16T18:28:24.000Z\"}}\n\n";
        AssistantStreamDataDto dto = assistantService.parseStreamChunk(raw);
        assertNotNull(dto);
        assertEquals(AssistantStreamFlag.START, dto.getFlag());
        assertTrue(dto.getData().contains("2026-08-16T18:28:24.000Z"));
    }

    @Test
    void testParseContentChunk() {
        String raw = "data: {\"flag\":\"CHUNK\",\"data\":{\"content\":\"Hello there!\"}}\n\n";
        AssistantStreamDataDto dto = assistantService.parseStreamChunk(raw);
        assertNotNull(dto);
        assertEquals(AssistantStreamFlag.CHUNK, dto.getFlag());
        assertTrue(dto.getData().contains("Hello there!"));
    }

    @Test
    void testParseDoneChunk() {
        String raw = "data: {\"flag\":\"DONE\",\"data\":null}\n\n";
        AssistantStreamDataDto dto = assistantService.parseStreamChunk(raw);
        assertNotNull(dto);
        assertEquals(AssistantStreamFlag.DONE, dto.getFlag());
        assertNull(dto.getData());
    }

    @Test
    void testParseEmptyOrDoneSignal() {
        assertNull(assistantService.parseStreamChunk(""));
        assertNull(assistantService.parseStreamChunk("   "));
        assertNull(assistantService.parseStreamChunk("data: [DONE]"));
        assertNull(assistantService.parseStreamChunk("invalid json"));
    }

    @Test
    void testFullStreamLifecycle() {
        User user = new User();
        user.setId(UUID.randomUUID());

        AssistantChatRoom room = new AssistantChatRoom(user);
        room.setId(UUID.randomUUID());

        AssistantChat userChat = new AssistantChat(room, user, AssistantChatType.USER, null, "Explain recursion");
        userChat.setId(UUID.randomUUID());

        AssistantService.AssistantStreamSession session =
                new AssistantService.AssistantStreamSession(room, user, userChat);

        // 1. START
        String startChunk = "data: {\"flag\":\"START\",\"data\":{\"timestamp\":\"2026-08-16T18:28:24.000Z\"}}\n\n";
        assistantService.handleStreamChunk(session, startChunk);

        assertNotNull(session.getAssistantChat());
        assertEquals(AssistantChatType.ASSISTANT, session.getAssistantChat().getType());
        assertEquals(AssistantChatGenerateStatus.RUNNING, session.getAssistantChat().getGenerateStatus());
        assertEquals(userChat.getId(), session.getAssistantChat().getUserChatId());

        // 2. CHUNKs
        String chunk1 = "data: {\"flag\":\"CHUNK\",\"data\":{\"content\":\"Recursion is \"}}\n\n";
        String chunk2 = "data: {\"flag\":\"CHUNK\",\"data\":{\"content\":\"a method of solving problems.\"}}\n\n";
        assistantService.handleStreamChunk(session, chunk1);
        assistantService.handleStreamChunk(session, chunk2);

        assertEquals("Recursion is a method of solving problems.", session.getContentBuilder().toString());

        // 3. METADATA
        String metadataChunk = "data: {\"flag\":\"METADATA\",\"data\":{\"title\":\"Understanding Recursion\",\"summary\":\"Explanation of recursion.\"}}\n\n";
        assistantService.handleStreamChunk(session, metadataChunk);

        assertEquals("Understanding Recursion", room.getTitle());
        assertEquals("Explanation of recursion.", room.getChatSummary());
        verify(assistantChatRoomRepository, times(1)).save(room);

        // 4. DONE
        String doneChunk = "data: {\"flag\":\"DONE\",\"data\":null}\n\n";
        assistantService.handleStreamChunk(session, doneChunk);

        assertEquals("Recursion is a method of solving problems.", session.getAssistantChat().getMessage());
        assertEquals(AssistantChatGenerateStatus.COMPLETED, session.getAssistantChat().getGenerateStatus());
        assertNotNull(session.getAssistantChat().getGeneratedAt());
    }

    @Test
    void testMetadataDoesNotOverwriteExistingTitle() {
        User user = new User();
        user.setId(UUID.randomUUID());

        AssistantChatRoom room = new AssistantChatRoom(user);
        room.setId(UUID.randomUUID());
        room.setTitle("Existing Title");

        AssistantChat userChat = new AssistantChat(room, user, AssistantChatType.USER, null, "Hello");
        userChat.setId(UUID.randomUUID());

        AssistantService.AssistantStreamSession session =
                new AssistantService.AssistantStreamSession(room, user, userChat);

        String metadataChunk = "data: {\"flag\":\"METADATA\",\"data\":{\"title\":\"New Generated Title\",\"summary\":\"New Summary\"}}\n\n";
        assistantService.handleStreamChunk(session, metadataChunk);

        assertEquals("Existing Title", room.getTitle());
        assertEquals("New Summary", room.getChatSummary());
        verify(assistantChatRoomRepository, times(1)).save(room);
    }

    @Test
    void testMetadataOverwritesOldSummaryEveryTime() {
        User user = new User();
        user.setId(UUID.randomUUID());

        AssistantChatRoom room = new AssistantChatRoom(user);
        room.setId(UUID.randomUUID());
        room.setChatSummary("Old Summary");

        AssistantChat userChat = new AssistantChat(room, user, AssistantChatType.USER, null, "Follow-up question");
        userChat.setId(UUID.randomUUID());

        AssistantService.AssistantStreamSession session =
                new AssistantService.AssistantStreamSession(room, user, userChat);

        String metadataChunk = "data: {\"flag\":\"METADATA\",\"data\":{\"title\":\"Updated Title\",\"summary\":\"Updated Summary after follow-up\"}}\n\n";
        assistantService.handleStreamChunk(session, metadataChunk);

        assertEquals("Updated Summary after follow-up", room.getChatSummary());
        verify(assistantChatRoomRepository, times(1)).save(room);
    }

    @Test
    void testErrorEventSetsStatusAndMessage() {
        User user = new User();
        user.setId(UUID.randomUUID());

        AssistantChatRoom room = new AssistantChatRoom(user);
        room.setId(UUID.randomUUID());

        AssistantChat userChat = new AssistantChat(room, user, AssistantChatType.USER, null, "Help");
        userChat.setId(UUID.randomUUID());

        AssistantService.AssistantStreamSession session =
                new AssistantService.AssistantStreamSession(room, user, userChat);

        // START
        assistantService.handleStreamChunk(session, "data: {\"flag\":\"START\",\"data\":{}}\n\n");

        // ERROR
        String errorChunk = "data: {\"flag\":\"ERROR\",\"data\":{\"error\":\"Rate limit exceeded\"}}\n\n";
        assistantService.handleStreamChunk(session, errorChunk);

        assertEquals("Rate limit exceeded", session.getAssistantChat().getErrorMessage());
        assertEquals(AssistantChatGenerateStatus.FAILED, session.getAssistantChat().getGenerateStatus());

        // Subsequent DONE should not overwrite FAILED status
        assistantService.handleStreamChunk(session, "data: {\"flag\":\"DONE\",\"data\":null}\n\n");
        assertEquals(AssistantChatGenerateStatus.FAILED, session.getAssistantChat().getGenerateStatus());
    }

    @Test
    void testChunkWithPlainStringData() {
        User user = new User();
        AssistantChatRoom room = new AssistantChatRoom(user);
        AssistantChat userChat = new AssistantChat(room, user, AssistantChatType.USER, null, "Help");
        AssistantService.AssistantStreamSession session =
                new AssistantService.AssistantStreamSession(room, user, userChat);

        assistantService.handleStreamChunk(session, "data: {\"flag\":\"CHUNK\",\"data\":\"Plain text chunk\"}\n\n");
        assertEquals("Plain text chunk", session.getContentBuilder().toString());
    }

    @Test
    void testAskAssistantCreatesNewRoomAndStreams() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        AssistantAskDto askDto = new AssistantAskDto();
        askDto.setMessage("How to learn Java?");
        askDto.setContexts(new ArrayList<>());
        askDto.setRoomId(null);

        LLMAssistantChatContextDto contextDto = new LLMAssistantChatContextDto();
        when(llmContextService.getAssistantChatContext(eq(user), any(), eq("How to learn Java?"))).thenReturn(contextDto);

        String chunkStart = "data: {\"flag\":\"START\",\"data\":{}}\n\n";
        String chunkText = "data: {\"flag\":\"CHUNK\",\"data\":{\"content\":\"Java is great!\"}}\n\n";
        String chunkDone = "data: {\"flag\":\"DONE\",\"data\":null}\n\n";

        when(llmApiService.getAssistantChatStream(contextDto)).thenReturn(Flux.just(chunkStart, chunkText, chunkDone));

        Flux<String> resultFlux = assistantService.askAssistant(askDto);
        List<String> emitted = resultFlux.collectList().block();

        assertNotNull(emitted);
        assertEquals(3, emitted.size());

        // Verify userChat was saved
        verify(assistantChatRepository, atLeastOnce()).save(any(AssistantChat.class));
        // Verify room was created and saved
        verify(assistantChatRoomRepository, atLeastOnce()).save(any(AssistantChatRoom.class));
    }

    @Test
    void testAskAssistantUsesExistingRoom() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        UUID existingRoomId = UUID.randomUUID();
        AssistantChatRoom existingRoom = new AssistantChatRoom(user);
        existingRoom.setId(existingRoomId);
        existingRoom.setTitle("Existing Room");

        when(assistantChatRoomRepository.findByIdAndUser(existingRoomId, user)).thenReturn(existingRoom);

        AssistantAskDto askDto = new AssistantAskDto();
        askDto.setMessage("Another question");
        askDto.setRoomId(existingRoomId);

        LLMAssistantChatContextDto contextDto = new LLMAssistantChatContextDto();
        when(llmContextService.getAssistantChatContext(eq(user), any(), eq("Another question"))).thenReturn(contextDto);
        when(llmApiService.getAssistantChatStream(contextDto)).thenReturn(Flux.just("data: {\"flag\":\"START\",\"data\":{}}\n\n"));

        Flux<String> resultFlux = assistantService.askAssistant(askDto);
        List<String> emitted = resultFlux.collectList().block();

        assertNotNull(emitted);
        assertEquals(1, emitted.size());
        verify(assistantChatRoomRepository, times(1)).findByIdAndUser(existingRoomId, user);
    }

    @Test
    void testAskAssistantThrowsWhenRoomNotFound() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        UUID nonExistentRoomId = UUID.randomUUID();
        when(assistantChatRoomRepository.findByIdAndUser(nonExistentRoomId, user)).thenReturn(null);

        AssistantAskDto askDto = new AssistantAskDto();
        askDto.setMessage("Hello");
        askDto.setRoomId(nonExistentRoomId);

        assertThrows(AssistantChatRoomNotFoundException.class, () -> assistantService.askAssistant(askDto));
    }

    @Test
    void testGetAssistantChatsSuccess() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        UUID roomId = UUID.randomUUID();
        AssistantChatRoom room = new AssistantChatRoom(user);
        room.setId(roomId);
        when(assistantChatRoomRepository.findByIdAndUser(roomId, user)).thenReturn(room);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        AssistantChat chat1 = new AssistantChat(room, user, AssistantChatType.USER, null, "Hello");
        chat1.setId(UUID.randomUUID());
        AssistantChat chat2 = new AssistantChat(room, user, AssistantChatType.ASSISTANT, chat1.getId(), AssistantChatGenerateStatus.COMPLETED);
        chat2.setId(UUID.randomUUID());
        chat2.setMessage("Hi! How can I help?");

        org.springframework.data.domain.Page<AssistantChat> chatPage =
                new org.springframework.data.domain.PageImpl<>(List.of(chat1, chat2));

        when(assistantChatRepository.findByChatRoomAndUser(room, user, pageable)).thenReturn(chatPage);

        org.springframework.data.domain.Page<AssistantChatDto> result = assistantService.getAssistantChats(roomId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Hello", result.getContent().get(0).getMessage());
        assertEquals("Hi! How can I help?", result.getContent().get(1).getMessage());
        verify(assistantChatRepository, times(1)).findByChatRoomAndUser(room, user, pageable);
    }

    @Test
    void testGetAssistantChatsThrowsWhenRoomNotFound() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        UUID roomId = UUID.randomUUID();
        when(assistantChatRoomRepository.findByIdAndUser(roomId, user)).thenReturn(null);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        assertThrows(AssistantChatRoomNotFoundException.class, () -> assistantService.getAssistantChats(roomId, pageable));
    }
}
