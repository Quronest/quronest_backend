package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPersonalDataDto {
    @JsonProperty("interested_domains")
    private List<String> interestedDomains = new ArrayList<>();

    @JsonProperty("skills")
    private List<String> skills = new ArrayList<>();

    @JsonProperty("primary_goal")
    @Size(max = 1000, message = "Primary goal should not exceed 1000 characters.")
    private String primaryGoal;

    @JsonProperty("experience")
    @Size(max = 1000, message = "Experience should not exceed 1000 characters")
    private String experience;

    @JsonProperty("description")
    @Size(max = 5000, message = "Description should not exceed 5000 characters")
    private String description;
}
