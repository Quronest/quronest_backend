package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.dto.llm.DailyTaskPlanLLMResponseDto;
import com.quronest.quronest_backend.model.enums.DailyTaskLevel;
import com.quronest.quronest_backend.model.enums.DailyTaskStatus;
import com.quronest.quronest_backend.model.enums.DailyTaskType;
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
@Table(name = "daily_task")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyTask {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private DailyPlan plan;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Ordering inside plan
    @Column(name = "task_order")
    private Integer order;

    // Basic Info
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private DailyTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain")
    private Domain domain;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_tags", columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private DailyTaskLevel level;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb")
    private JsonNode content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DailyTaskStatus status = DailyTaskStatus.PENDING;

    @Column(name = "expected_total_time")
    private Integer expectedTotalTime;

    @Column(name = "actual_time_spent")
    private Integer actualTimeSpent = 0;

    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Column(name = "is_optional")
    private Boolean isOptional = false;

    // Versioning
    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public DailyTask(DailyPlan plan, User user, DailyTaskPlanLLMResponseDto llmResponseDto) {
        this.plan = plan;
        this.user = user;
        this.order = llmResponseDto.getTaskNumber();
        this.title = llmResponseDto.getTitle();
        this.description = llmResponseDto.getDescription();
        this.taskType = llmResponseDto.getType();
        this.expectedTotalTime = llmResponseDto.getExpectedTotalMinutes();
    }
}
