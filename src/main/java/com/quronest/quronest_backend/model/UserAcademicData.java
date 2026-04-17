package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAcademicData {
    @JsonProperty("institute_name")
    private String instituteName;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("course")
    private String course;

    @JsonProperty("description")
    private String description;
}
