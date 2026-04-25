package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_task_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyTaskLog {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private DailyTask task;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private DailyPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DailyTaskStatus status;

    // Time Tracking
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_time_spent")
    private Integer totalTimeSpent; // in minutes

    // Effort Tracking
    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "retries")
    private Integer retries = 0;

    @Column(name = "hints_used")
    private Integer hintsUsed = 0;

    // Performance (Flexible for all task types)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "performance_data", columnDefinition = "jsonb")
    private Object performanceData;

    @Column(name = "confidence_level")
    private Integer confidenceLevel; // 1–5

    // AI Evaluation
    @Column(name = "ai_score")
    private Double aiScore;

    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;
}
