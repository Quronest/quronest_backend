package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.exception.UserNotFoundException;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.service.UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        User dbUser = null;
        try {
            dbUser = userService.getOrCreateOAuth2User(oidcUser.getAttribute("email"), oidcUser.getAttribute("name"),
                                                       oidcUser.getPicture());
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                    "Could not find or create user account with the email id: " + oidcUser.getAttribute("email"));
        }

        return new CustomOidcUser(oidcUser, dbUser);
    }

}