package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAbout {

    @JsonProperty("about")
    private String about;

    @JsonProperty("institute_name")
    private String instituteName;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("social_links")
    private List<SocialLink> socialLinks = new ArrayList<>();
}
