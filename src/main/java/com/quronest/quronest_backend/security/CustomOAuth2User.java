package com.quronest.quronest_backend.security;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@JsonIncludeProperties({"user", "oAuth2User"})
public class CustomOAuth2User extends AbstractUserAccount implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final SecurityUser securityUser;

    public CustomOAuth2User(@JsonProperty("oAuth2User") OAuth2User oAuth2User, @JsonProperty("user") User user) {
        this.oAuth2User = oAuth2User;
        this.user = user;
        this.securityUser = new SecurityUser(user);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return securityUser.getAuthorities();
    }

    @Override
    public String getName() {
        return oAuth2User.getAttribute("email");
    }

    // Required for json serialization
    public OAuth2User getoAuth2User() {
        return oAuth2User;
    }

}
