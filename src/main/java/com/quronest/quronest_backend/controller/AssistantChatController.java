package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.AssistantAskDto;
import com.quronest.quronest_backend.dto.AssistantChatDto;
import com.quronest.quronest_backend.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/assistant")
public class AssistantChatController {
    private final AssistantService assistantService;

    public AssistantChatController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping(value = "/chats/new", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> createNewAssistantChat(@Valid @RequestBody AssistantAskDto assistantAskDto) {
        return assistantService.askAssistant(assistantAskDto);
    }

    @GetMapping("/chats/{roomId}")
    public Page<AssistantChatDto> getAssistantChats(
            @PathVariable UUID roomId,
            @PageableDefault(size = 20, sort = "creationTimestamp", direction = Sort.Direction.ASC) Pageable pageable) {
        return assistantService.getAssistantChats(roomId, pageable);
    }
}
