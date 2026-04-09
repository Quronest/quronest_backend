package com.quronest.quronest_backend.security;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.table.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

@JsonIncludeProperties({"user", "oidcUser"})
public class CustomOidcUser extends AbstractUserAccount implements OidcUser {

    private final OidcUser oidcUser;
    private final SecurityUser securityUser;
    private static final Log logger = LogFactory.getLog(CustomOidcUser.class);

    public CustomOidcUser(@JsonProperty("oidcUser") OidcUser oidcUser, @JsonProperty("user") User user) {
        this.oidcUser = oidcUser;
        this.user = user;
        this.securityUser = new SecurityUser(user);
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return securityUser.getAuthorities();
    }

    @Override
    public String getName() {
        return oidcUser.getAttribute("email");
    }

    // Required for json serialization
    public OidcUser getOidcUser() {
        return oidcUser;
    }
}

