package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.dto.UserAcademicDataDto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserAcademicData {
    @JsonProperty("institute_name")
    private String instituteName;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("course")
    private String course;

    @JsonProperty("description")
    private String description;

    public UserAcademicData(UserAcademicDataDto academicDataDto) {
        this.instituteName = academicDataDto.getInstituteName();
        this.grade = academicDataDto.getGrade();
        this.course = academicDataDto.getCourse();
        this.description = academicDataDto.getDescription();
    }
}
