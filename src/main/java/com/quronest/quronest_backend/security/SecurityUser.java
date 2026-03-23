package com.quronest.quronest_backend.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.table.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityUser extends AbstractUserAccount implements UserDetails {
    @JsonCreator
    public SecurityUser(@JsonProperty("user") User user) {
        this.user = user;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(user.getRoles().split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @JsonIgnore
    @Override
    public @Nullable String getPassword() {
        return this.user.getPassword();
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return this.user.isEmailVerified();
    }

}
