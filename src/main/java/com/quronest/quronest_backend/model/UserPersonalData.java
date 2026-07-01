package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.dto.UserPersonalDataDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserPersonalData {
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

    public UserPersonalData(UserPersonalDataDto personalDataDto) {
        this.interestedDomains = personalDataDto.getInterestedDomains();
        this.skills = personalDataDto.getSkills();
        this.primaryGoal = personalDataDto.getPrimaryGoal();
        this.experience = personalDataDto.getExperience();
        this.description = personalDataDto.getDescription();
    }
}
