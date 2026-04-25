package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.SocialType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialLink {
    @JsonProperty("link")
    private String link = "";

    @JsonProperty("type")
    private SocialType type = SocialType.OTHER;

    @JsonProperty("title")
    private String title = "";

    public SocialLink(String link, SocialType type, String title) {
        this.link = link;
        this.type = type;
        this.title = title;
    }
}
