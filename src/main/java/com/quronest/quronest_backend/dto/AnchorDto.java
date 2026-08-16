package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.AnchorOffset;
import com.quronest.quronest_backend.model.enums.AnchorType;
import com.quronest.quronest_backend.model.table.Anchor;
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
public class AnchorDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("reference_id")
    private UUID referenceId;

    @JsonProperty("type")
    private AnchorType type;

    @JsonProperty("block_offset")
    private AnchorOffset blockOffset;

    @JsonProperty("selection_offset")
    private AnchorOffset selectionOffset;

    @JsonProperty("selected_text")
    private String selectedText;

    @JsonProperty("creation_timestamp")
    private LocalDateTime creationTimestamp;

    @JsonProperty("update_timestamp")
    private LocalDateTime updateTimestamp;

    public AnchorDto(Anchor anchor) {
        this.id = anchor.getId();
        this.referenceId = anchor.getReferenceId();
        this.type = anchor.getType();
        this.blockOffset = anchor.getBlockOffset();
        this.selectionOffset = anchor.getSelectionOffset();
        this.selectedText = anchor.getSelectedText();
    }
}
