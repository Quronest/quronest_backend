package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quronest.quronest_backend.model.*;
import com.quronest.quronest_backend.model.enums.UserAccountStatus;
import com.quronest.quronest_backend.validation.ValidEmail;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "email", unique = true)
    @NonNull
    @ValidEmail
    private String email;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "roles")
    private String roles;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private UserAccountStatus accountStatus;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "phone")
    private String phone;

    @Column(name = "phone_verified")
    private boolean phoneVerified;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "other_data", columnDefinition = "jsonb")
    private UserAbout about = new UserAbout();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "academic_data", columnDefinition = "jsonb")
    private UserAcademicData academicData = new UserAcademicData();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "personal_data", columnDefinition = "jsonb")
    private UserPersonalData personalData = new UserPersonalData();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "internal_data", columnDefinition = "jsonb")
    private UserInternalData internalData = new UserInternalData();

    @Column(name = "blacklisted")
    private boolean blacklisted = false;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public User(String fullname, String email) {
        this.fullname = fullname;
        this.email = email;
    }

    public boolean isProfileComplete() {
        return getAccountStatus() == UserAccountStatus.COMPLETE;
    }
}
