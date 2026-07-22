package com.quronest.quronest_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.UserAbout;
import com.quronest.quronest_backend.model.UserAcademicData;
import com.quronest.quronest_backend.model.UserCurrentSummary;
import com.quronest.quronest_backend.model.UserPersonalData;
import com.quronest.quronest_backend.model.enums.UserAccountStatus;
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

    @JsonProperty("account_status")
    private UserAccountStatus accountStatus;

    @JsonProperty("other_data")
    private UserAbout about = new UserAbout();

    @JsonProperty("personal_data")
    private UserPersonalData personalDataDto;

    @JsonProperty("academic_data")
    private UserAcademicData academicDataDto;

    @JsonProperty("current_summary")
    private UserCurrentSummary currentSummary;

    public UserProfileDto(User user) {
        this.id = user.getId();
        this.fullname = user.getFullname();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.avatar = user.getAvatar();
        this.emailVerified = user.isEmailVerified();
        this.phoneVerified = user.isPhoneVerified();
        this.accountStatus = user.getAccountStatus();
        this.phone = user.getPhone();
        this.about = user.getAbout();
        this.personalDataDto = user.getPersonalData();
        this.academicDataDto = user.getAcademicData();
        this.currentSummary = user.getCurrentSummary();
    }
}
