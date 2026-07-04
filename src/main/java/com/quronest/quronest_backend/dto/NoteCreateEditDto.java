package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoteCreateEditDto {
    @JsonProperty("reference_text")
    @Size(max = 10000, message = "Reference should not exceed 10000 characters")
    private String referenceText;

    @JsonProperty("message")
    @Size(max = 10000, message = "Message should not exceed 10000 characters")
    private String message;
}
