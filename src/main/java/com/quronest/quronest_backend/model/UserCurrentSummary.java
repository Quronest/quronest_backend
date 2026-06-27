package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserCurrentSummary {
    @JsonProperty("group")
    private UserGroup group;

    @JsonProperty("phase")
    private UserPhase phase;

    @JsonProperty("summary")
    private String summary;

    public void setCurrentSummary(UserGroupSummaryDto userGroupSummaryDto) {
        this.group = userGroupSummaryDto.getGroup();
        this.phase = userGroupSummaryDto.getPhase();
        this.summary = userGroupSummaryDto.getSummary();
    }
}
