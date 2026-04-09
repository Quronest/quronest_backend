package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.config.SecurityParameters;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    private final CookieConfig cookieConfig;
    private final SecurityParameters securityParameters;

    public CustomOAuth2SuccessHandler(JwtService jwtService, CookieUtils cookieUtils, CookieConfig cookieConfig,
                                      SecurityParameters securityParameters) {
        this.jwtService = jwtService;
        this.cookieUtils = cookieUtils;
        this.cookieConfig = cookieConfig;
        this.securityParameters = securityParameters;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

        SecurityUser principal = new SecurityUser(oauthUser.getUser());

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        cookieUtils.addCookie(response, "accessToken", accessToken, cookieConfig.getAccessTokenMaxAge());
        cookieUtils.addCookie(response, "refreshToken", refreshToken, cookieConfig.getRefreshTokenMaxAge());

        String redirectUri = (String) request.getSession().getAttribute("REDIRECT_URI");

        if (redirectUri == null || redirectUri.isBlank()) {
            redirectUri = securityParameters.getHomepage(); // fallback
        }

        // Cleanup session
        request.getSession().removeAttribute("REDIRECT_URI");

        // Redirect user
        response.sendRedirect(redirectUri);
    }
}
