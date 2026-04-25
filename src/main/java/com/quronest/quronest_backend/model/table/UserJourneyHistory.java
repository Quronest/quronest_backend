package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.enums.JourneyEventTriggerer;
import com.quronest.quronest_backend.model.enums.JourneyEventType;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_journey_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserJourneyHistory {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private JourneyEventType eventType;

    // Previous State
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_group")
    private UserGroup previousGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_phase")
    private UserPhase previousPhase;

    // New State
    @Enumerated(EnumType.STRING)
    @Column(name = "new_group")
    private UserGroup newGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_phase")
    private UserPhase newPhase;

    // Why this happened
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // Flexible Metadata (scores, domain, etc.)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Object metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by")
    private JourneyEventTriggerer triggeredBy;

    // Confidence Score
    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;
}
