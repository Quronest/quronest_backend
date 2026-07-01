package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.UserBurnoutRisk;
import com.quronest.quronest_backend.model.enums.UserEngagementLevel;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import com.quronest.quronest_backend.model.table.UserJourney;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserJourneyDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("group")
    private UserGroup group;

    @JsonProperty("phase")
    private UserPhase phase;

    @JsonProperty("current_day")
    private Integer currentDay;

    @JsonProperty("streak_days")
    private Integer streakDays;

    @JsonProperty("total_active_days")
    private Integer totalActiveDays;

    @JsonProperty("last_active_at")
    private LocalDateTime lastActiveAt;

    @JsonProperty("current_stage")
    private String currentStage;

    @JsonProperty("engagement_level")
    private UserEngagementLevel engagementLevel;

    @JsonProperty("burnout_risk")
    private UserBurnoutRisk burnoutRisk;

    @JsonProperty("is_on_track")
    private Boolean isOnTrack;

    @JsonProperty("needs_intervention")
    private Boolean needsIntervention;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("creation_timestamp")
    private LocalDateTime creationTimestamp;

    @JsonProperty("update_timestamp")
    private LocalDateTime updateTimestamp;

    public UserJourneyDto(UserJourney journey) {
        this.id = journey.getId();
        if (journey.getUser() != null) {
            this.userId = journey.getUser().getId();
        }
        this.group = journey.getGroup();
        this.phase = journey.getPhase();
        this.currentDay = journey.getCurrentDay();
        this.streakDays = journey.getStreakDays();
        this.totalActiveDays = journey.getTotalActiveDays();
        this.lastActiveAt = journey.getLastActiveAt();
        this.currentStage = journey.getCurrentStage();
        this.engagementLevel = journey.getEngagementLevel();
        this.burnoutRisk = journey.getBurnoutRisk();
        this.isOnTrack = journey.getIsOnTrack();
        this.needsIntervention = journey.getNeedsIntervention();
        this.summary = journey.getSummary();
        this.creationTimestamp = journey.getCreationTimestamp();
        this.updateTimestamp = journey.getUpdateTimestamp();
    }
}
