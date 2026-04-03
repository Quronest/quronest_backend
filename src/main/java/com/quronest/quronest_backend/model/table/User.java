package com.quronest.quronest_backend.model.table;

import com.quronest.quronest_backend.model.UserAbout;
import com.quronest.quronest_backend.model.UserAccountStatus;
import com.quronest.quronest_backend.validation.ValidEmail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "email", unique = true)
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
