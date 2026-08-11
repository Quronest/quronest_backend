package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.table.Note;
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
public class NoteDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("task_id")
    private UUID taskId;

    @JsonProperty("reference_text")
    private String referenceText;

    @JsonProperty("message")
    private String message;

    @JsonProperty("anchor")
    private AnchorDto anchor;

    @JsonProperty("creation_timestamp")
    private LocalDateTime creationTimestamp;

    @JsonProperty("update_timestamp")
    private LocalDateTime updateTimestamp;

    public NoteDto(Note note) {
        this.id = note.getId();
        if (note.getTask() != null) {
            this.taskId = note.getTask().getId();
        }
        this.referenceText = note.getReferenceText();
        this.message = note.getMessage();
        if (note.getAnchor() != null) {
            this.anchor = new AnchorDto(note.getAnchor());
        }
        this.creationTimestamp = note.getCreationTimestamp();
        this.updateTimestamp = note.getUpdateTimestamp();
    }
}
