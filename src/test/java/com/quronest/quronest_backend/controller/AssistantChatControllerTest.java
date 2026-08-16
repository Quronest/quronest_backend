package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.dto.AssistantAskDto;
import com.quronest.quronest_backend.dto.AssistantChatDto;
import com.quronest.quronest_backend.service.AssistantService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AssistantChatControllerTest {

    private AssistantService assistantService;
    private AssistantChatController assistantChatController;

    @BeforeEach
    void setUp() {
        assistantService = Mockito.mock(AssistantService.class);
        assistantChatController = new AssistantChatController(assistantService);
    }

    @Test
    void testCreateNewAssistantChat() {
        AssistantAskDto askDto = new AssistantAskDto();
        askDto.setMessage("Hello");
        askDto.setContexts(new ArrayList<>());

        when(assistantService.askAssistant(askDto)).thenReturn(Flux.just("chunk1", "chunk2"));

        Flux<String> responseStream = assistantChatController.createNewAssistantChat(askDto);
        List<String> collected = responseStream.collectList().block();

        assertNotNull(collected);
        assertEquals(2, collected.size());
        assertEquals("chunk1", collected.get(0));
        assertEquals("chunk2", collected.get(1));

        verify(assistantService, times(1)).askAssistant(askDto);
    }

    @Test
    void testGetAssistantChats() {
        UUID roomId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        AssistantChatDto chatDto = new AssistantChatDto();
        chatDto.setMessage("Hello response");
        Page<AssistantChatDto> mockPage = new PageImpl<>(List.of(chatDto));

        when(assistantService.getAssistantChats(roomId, pageable)).thenReturn(mockPage);

        Page<AssistantChatDto> result = assistantChatController.getAssistantChats(roomId, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Hello response", result.getContent().get(0).getMessage());

        verify(assistantService, times(1)).getAssistantChats(roomId, pageable);
    }
}
