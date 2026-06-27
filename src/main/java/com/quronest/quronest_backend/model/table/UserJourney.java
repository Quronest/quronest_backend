package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.enums.UserBurnoutRisk;
import com.quronest.quronest_backend.model.enums.UserEngagementLevel;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_journey")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserJourney {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_name", nullable = false)
    private UserGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false)
    private UserPhase phase;

    @Column(name = "current_day")
    private Integer currentDay; // Day inside phase

    @Column(name = "streak_days")
    private Integer streakDays = 0;

    @Column(name = "total_active_days")
    private Integer totalActiveDays = 0;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    // AI Contexts
    @Column(name = "current_stage")
    private String currentStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "engagement_level")
    private UserEngagementLevel engagementLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "burnout_risk")
    private UserBurnoutRisk burnoutRisk;

    @Column(name = "is_on_track")
    private Boolean isOnTrack = true;

    @Column(name = "needs_intervention")
    private Boolean needsIntervention = false;

    @Column(name = "summary")
    private String summary;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public UserJourney(User user, UserGroup group, UserPhase phase, String summary) {
        this.user = user;
        this.group = group;
        this.phase = phase;
        this.summary = summary;
    }
}
