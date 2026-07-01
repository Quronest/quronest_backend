package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class UserAbout {

    @JsonProperty("about")
    private String about;

    @JsonProperty("social_links")
    private List<SocialLink> socialLinks = new ArrayList<>();
}
