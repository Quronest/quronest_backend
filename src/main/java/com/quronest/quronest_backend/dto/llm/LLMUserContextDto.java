package com.quronest.quronest_backend.dto.llm;

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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LLMUserContextDto {
    @JsonProperty("current_group")
    private UserGroup currentGroup;

    @JsonProperty("current_phase")
    private UserPhase currentPhase;

    @JsonProperty("current_stage")
    private String currentStage;

    @JsonProperty("current_day")
    private Integer currentDay;

    @JsonProperty("engagement_level")
    private UserEngagementLevel engagementLevel;

    @JsonProperty("burnout_risk")
    private UserBurnoutRisk burnoutRisk;

    @JsonProperty("is_on_track")
    private Boolean isOnTrack = true;

    @JsonProperty("needs_intervention")
    private Boolean needsIntervention = false;

    @JsonProperty("summary")
    private String summary;

    public LLMUserContextDto(UserJourney journey) {
        this.currentGroup = journey.getGroup();
        this.currentPhase = journey.getPhase();
        this.summary = journey.getSummary();
        this.currentStage = journey.getCurrentStage();
        this.currentDay = journey.getCurrentDay();
        this.engagementLevel = journey.getEngagementLevel();
        this.burnoutRisk = journey.getBurnoutRisk();
        this.isOnTrack = journey.getIsOnTrack();
        this.needsIntervention = journey.getNeedsIntervention();
    }
}
