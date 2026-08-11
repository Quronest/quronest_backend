package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.AnchorOffset;
import com.quronest.quronest_backend.model.enums.AnchorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnchorCreateDto {
    @JsonProperty("type")
    private AnchorType type;

    @JsonProperty("block_offset")
    private AnchorOffset blockOffset;

    @JsonProperty("selection_offset")
    private AnchorOffset selectionOffset;

    @JsonProperty("selected_text")
    private String selectedText;
}
