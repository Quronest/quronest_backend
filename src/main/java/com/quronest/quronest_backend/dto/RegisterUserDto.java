package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.validation.ValidEmail;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDto {

    @NotEmpty(message = "Fullname must be provided.")
    @JsonProperty("fullname")
    private String fullname;

    @NotEmpty(message = "Username must be provided.")
    @JsonProperty("username")
    private String username;

    @ValidEmail(message = "Provide a valid email.")
    @NotEmpty(message = "Email must be provided.")
    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

}
