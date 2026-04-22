package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.UserAcademicData;
import com.quronest.quronest_backend.model.UserPersonalData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupSummaryGenerateDto {
    @JsonProperty("academic_data")
    private UserAcademicData academicData;

    @JsonProperty("personal_data")
    private UserPersonalData personalData;
}
