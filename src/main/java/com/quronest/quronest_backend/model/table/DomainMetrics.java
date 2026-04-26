package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.enums.Domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "domain_metrics")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainMetrics {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", nullable = false)
    private Domain domain;

    // Sub-domains
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sub_domains", columnDefinition = "jsonb")
    private List<String> subDomains = new ArrayList<>();

    // CORE SCORES (0–100)
    @Column(name = "engagement_score")
    private Double engagementScore = 0.0;

    @Column(name = "consistency_score")
    private Double consistencyScore = 0.0;

    @Column(name = "problem_solving_score")
    private Double problemSolvingScore = 0.0;

    @Column(name = "completion_score")
    private Double completionScore = 0.0;

    @Column(name = "curiosity_score")
    private Double curiosityScore = 0.0;

    // FINAL SCORE
    @Column(name = "affinity_score")
    private Double affinityScore = 0.0;

    // AGGREGATED DATA
    @Column(name = "total_tasks")
    private Integer totalTasks = 0;

    @Column(name = "completed_tasks")
    private Integer completedTasks = 0;

    @Column(name = "skipped_tasks")
    private Integer skippedTasks = 0;

    @Column(name = "total_time_spent")
    private Integer totalTimeSpent = 0;

    @Column(name = "total_attempts")
    private Integer totalAttempts = 0;

    @Column(name = "total_retries")
    private Integer totalRetries = 0;

    // AVERAGES
    @Column(name = "avg_attempts_per_task")
    private Double avgAttemptsPerTask = 0.0;

    @Column(name = "avg_time_per_task")
    private Double avgTimePerTask = 0.0;

    // STRUGGLE SIGNALS
    @Column(name = "high_retry_count")
    private Integer highRetryCount = 0;

    @Column(name = "skip_count")
    private Integer skipCount = 0;

    // TREND
    @Column(name = "improvement_trend")
    private Double improvementTrend = 0.0; // -1 to +1

    // Activity Tracking
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;
}
