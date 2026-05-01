package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.Constants;
import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.config.SecurityParameters;
import com.quronest.quronest_backend.model.table.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomOAuth2SuccessHandlerTest {

    @Test
    void acceptsAnyOAuthPrincipalThatExposesUserAccount() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refresh-token");

        CookieConfig cookieConfig = new CookieConfig();
        CookieUtils cookieUtils = new CookieUtils(cookieConfig);
        SecurityParameters securityParameters = new SecurityParameters();
        securityParameters.setHomepage("https://localhost.quronest.com/home");

        CustomOAuth2SuccessHandler handler =
                new CustomOAuth2SuccessHandler(jwtService, cookieUtils, cookieConfig, securityParameters);

        User user = new User("Test User", "test@example.com");
        user.setRoles(Constants.USER_ROLE_DEFAULT);

        UserAccount principal = mock(UserAccount.class);
        when(principal.getUser()).thenReturn(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("REDIRECT_URI", "https://localhost.quronest.com/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("https://localhost.quronest.com/dashboard");
        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(cookie -> assertThat(cookie).contains("accessToken=access-token"))
                .anySatisfy(cookie -> assertThat(cookie).contains("refreshToken=refresh-token"));
    }
}
