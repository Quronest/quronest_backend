package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SelectionAnchor {
    @JsonProperty("reference_id")
    private UUID referenceId;

    @JsonProperty("block_offset")
    private BlockOffset blockOffset;

    @JsonProperty("selection_offset")
    private SelectionOffset selectionOffset;

    @JsonProperty("selected_text")
    private String selectedText;

    @Getter
    @AllArgsConstructor
    public static class BlockOffset {
        @JsonProperty("start")
        private Integer start;

        @JsonProperty("end")
        private Integer end;
    }

    @Getter
    @AllArgsConstructor
    public static class SelectionOffset {
        @JsonProperty("start")
        private Integer start;

        @JsonProperty("end")
        private Integer end;
    }
}
