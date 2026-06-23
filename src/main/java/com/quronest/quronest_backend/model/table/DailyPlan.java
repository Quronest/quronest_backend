package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.enums.DailyPlanStatus;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_plan")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyPlan {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "group")
    private UserGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase")
    private UserPhase phase;

    // Plan Details
    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "is_base_plan")
    private Boolean isBasePlan = true;

    @Column(name = "is_adjusted")
    private Boolean isAdjusted = false;

    @Column(name = "adjustment_reason")
    private String adjustmentReason;

    // progress track
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DailyPlanStatus status;

    @Column(name = "expected_total_time")
    private Integer expectedTotalTime;

    @Column(name = "actual_time_spent")
    private Integer actualTimeSpent;

    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    // Versioning
    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;
}
