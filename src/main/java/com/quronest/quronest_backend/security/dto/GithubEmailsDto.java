package com.quronest.quronest_backend.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubEmailsDto {
    @JsonProperty("email")
    private String email;

    @JsonProperty("primary")
    private Boolean primary;

    @JsonProperty("verified")
    private Boolean verified;
}
