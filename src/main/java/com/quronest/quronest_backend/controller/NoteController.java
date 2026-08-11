package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.config.Urls;
import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.NoteCreateDto;
import com.quronest.quronest_backend.dto.NoteEditDto;
import com.quronest.quronest_backend.dto.NoteDto;
import com.quronest.quronest_backend.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping(Urls.API_BASE_URL + "/notes")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/{taskId}")
    public NoteDto createNewTaskNote(@PathVariable UUID taskId,
                                     @Valid @RequestBody NoteCreateDto noteCreateDto) {
        return noteService.createTaskNote(taskId, noteCreateDto);
    }

    @PatchMapping("/{noteId}")
    public NoteDto editNote(@PathVariable UUID noteId, @Valid @RequestBody NoteEditDto noteEditDto) {
        return noteService.editNote(noteId, noteEditDto);
    }

    @DeleteMapping("/{noteId}")
    public BooleanDto deleteNote(@PathVariable UUID noteId) {
        return noteService.deleteNote(noteId);
    }

    @GetMapping
    public Page<NoteDto> getNotes(
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @PageableDefault(size = 20, sort = "creationTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return noteService.getNotes(taskId, startTime, endTime, pageable);
    }
}
