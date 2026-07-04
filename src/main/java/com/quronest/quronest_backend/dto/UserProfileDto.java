package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.UserAbout;
import com.quronest.quronest_backend.model.UserCurrentSummary;
import com.quronest.quronest_backend.model.table.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("email_verified")
    private boolean emailVerified = false;

    @JsonProperty("phone_verified")
    private boolean phoneVerified = false;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("other_data")
    private UserAbout about = new UserAbout();

    @JsonProperty("personal_data")
    private UserPersonalDataDto personalDataDto = new UserPersonalDataDto();

    @JsonProperty("academic_data")
    private UserAcademicDataDto academicDataDto = new UserAcademicDataDto();

    @JsonProperty("current_summary")
    private UserCurrentSummary currentSummary = new UserCurrentSummary();

    public UserProfileDto(User user) {
        this.id = user.getId();
        this.fullname = user.getFullname();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.avatar = user.getAvatar();
        this.emailVerified = user.isEmailVerified();
        this.phoneVerified = user.isPhoneVerified();
        this.phone = user.getPhone();
        this.about = user.getAbout();
        this.personalDataDto = new UserPersonalDataDto(user.getPersonalData());
        this.academicDataDto = new UserAcademicDataDto(user.getAcademicData());
        this.currentSummary = user.getCurrentSummary();
    }
}
