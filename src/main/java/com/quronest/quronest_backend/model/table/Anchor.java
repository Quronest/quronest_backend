package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.AnchorOffset;
import com.quronest.quronest_backend.model.enums.AnchorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "anchor")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Anchor {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AnchorType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "block_offset", columnDefinition = "jsonb")
    private AnchorOffset blockOffset;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selection_offset", columnDefinition = "jsonb")
    private AnchorOffset selectionOffset;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_text")
    private String selectedText;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public Anchor(UUID referenceId, AnchorType type, AnchorOffset blockOffset, AnchorOffset selectionOffset,
                  String selectedText) {
        this.referenceId = referenceId;
        this.type = type;
        this.blockOffset = blockOffset;
        this.selectionOffset = selectionOffset;
        this.selectedText = selectedText;
    }
}
