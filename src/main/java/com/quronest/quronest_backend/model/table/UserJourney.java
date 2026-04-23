package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.UserBurnoutRisk;
import com.quronest.quronest_backend.model.UserEngagementLevel;
import com.quronest.quronest_backend.model.UserGroup;
import com.quronest.quronest_backend.model.UserPhase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @Column(name = "current_group", nullable = false)
    private UserGroup currentGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false)
    private UserPhase currentPhase;

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

    public UserJourney(User user, UserGroup currentGroup, UserPhase currentPhase, String summary) {
        this.user = user;
        this.currentGroup = currentGroup;
        this.currentPhase = currentPhase;
        this.summary = summary;
    }
}
