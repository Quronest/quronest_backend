package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.BooleanDto;
import com.quronest.quronest_backend.dto.NoteCreateEditDto;
import com.quronest.quronest_backend.dto.NoteDto;
import com.quronest.quronest_backend.exception.DailyTaskNotFoundException;
import com.quronest.quronest_backend.exception.NoteNotFoundException;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.Note;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.DailyTaskRepository;
import com.quronest.quronest_backend.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class NoteService {
    private final UserService userService;
    private final DailyTaskRepository dailyTaskRepository;
    private final NoteRepository noteRepository;

    public NoteService(UserService userService,
                       DailyTaskRepository dailyTaskRepository,
                       NoteRepository noteRepository) {
        this.userService = userService;
        this.dailyTaskRepository = dailyTaskRepository;
        this.noteRepository = noteRepository;
    }

    public NoteDto createTaskNote(UUID taskId, NoteCreateEditDto noteCreateEditDto) {
        User user = userService.getAuthenticatedUser();
        DailyTask task = dailyTaskRepository.findByIdAndUser(taskId, user);
        if (task == null) {
            throw new DailyTaskNotFoundException();
        }

        Note note = new Note(user, task, noteCreateEditDto.getReferenceText(), noteCreateEditDto.getMessage());
        noteRepository.save(note);

        return new NoteDto(note);
    }

    public NoteDto editNote(UUID noteId, NoteCreateEditDto noteCreateEditDto) {
        Note note = getNoteEditable(noteId);

        note.setMessage(noteCreateEditDto.getMessage());
        noteRepository.save(note);

        return new NoteDto(note);
    }

    public BooleanDto deleteNote(UUID noteId) {
        Note note = getNoteEditable(noteId);
        noteRepository.delete(note);

        return new BooleanDto(true);
    }

    private Note getNoteEditable(UUID noteId) {
        User user = userService.getAuthenticatedUser();
        Note note = noteRepository.findByIdAndUser(noteId, user);
        if (note == null) {
            throw new NoteNotFoundException();
        }
        return note;
    }

    public Page<NoteDto> getNotes(UUID taskId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        User user = userService.getAuthenticatedUser();
        Page<Note> notes = noteRepository.findNotesWithFilters(user, taskId, startTime, endTime, pageable);
        return notes.map(NoteDto::new);
    }
}
