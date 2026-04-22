package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String primaryGoal;

    @JsonProperty("experience")
    private String experience;

    @JsonProperty("description")
    private String description;
}
