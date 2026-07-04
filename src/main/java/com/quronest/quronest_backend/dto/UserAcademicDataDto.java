package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.UserAcademicData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAcademicDataDto {
    @JsonProperty("institute_name")
    private String instituteName;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("course")
    private String course;

    @JsonProperty("description")
    private String description;

    public UserAcademicDataDto(UserAcademicData academicData) {
        this.instituteName = academicData.getInstituteName();
        this.grade = academicData.getGrade();
        this.course = academicData.getCourse();
        this.description = academicData.getDescription();
    }
}
