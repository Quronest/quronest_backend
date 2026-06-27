package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.UserCurrentSummary;
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
public class UserGroupSummaryDto {
    @JsonProperty("group")
    private UserGroup group;

    @JsonProperty("phase")
    private UserPhase phase;

    @JsonProperty("summary")
    private String summary;

    public UserGroupSummaryDto(UserJourney journey) {
        this.group = journey.getGroup();
        this.phase = journey.getPhase();
        this.summary = journey.getSummary();
    }

    public UserGroupSummaryDto(UserCurrentSummary currentSummary) {
        this.group = currentSummary.getGroup();
        this.phase = currentSummary.getPhase();
        this.summary = currentSummary.getSummary();
    }
}
