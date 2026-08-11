package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "note")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private DailyTask task;

    @Column(name = "reference_text")
    private String referenceText;

    @Column(name = "message")
    private String message;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "anchor_id")
    private Anchor anchor;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public Note(User user, DailyTask task, String referenceText, String message) {
        this.user = user;
        this.task = task;
        this.referenceText = referenceText;
        this.message = message;
    }
}
