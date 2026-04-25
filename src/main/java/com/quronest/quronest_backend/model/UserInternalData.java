package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.dto.UserGroupSummaryDto;
import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInternalData {
    @JsonProperty("group")
    private UserGroup group;

    @JsonProperty("phase")
    private UserPhase phase;

    @JsonProperty("summary")
    private String summary;

    public void setUserGroupSummary(UserGroupSummaryDto userGroupSummaryDto) {
        this.group = userGroupSummaryDto.getGroup();
        this.phase = userGroupSummaryDto.getPhase();
        this.summary = userGroupSummaryDto.getSummary();
    }
}
