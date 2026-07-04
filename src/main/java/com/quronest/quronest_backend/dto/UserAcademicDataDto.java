package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
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
    @Size(max = 1000, message = "Grade should not exceed 1000 characters.")
    private String grade;

    @JsonProperty("course")
    @Size(max = 1000, message = "Course should not exceed 1000 characters.")
    private String course;

    @JsonProperty("description")
    @Size(max = 5000, message = "Description should not exceed 5000 characters")
    private String description;
}
