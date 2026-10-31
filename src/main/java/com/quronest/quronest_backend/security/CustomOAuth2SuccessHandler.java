package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.config.SecurityParameters;
import com.quronest.quronest_backend.dto.TokenPair;
import com.quronest.quronest_backend.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final CookieUtils cookieUtils;
    private final CookieConfig cookieConfig;
    private final SecurityParameters securityParameters;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public CustomOAuth2SuccessHandler(TokenService tokenService, CookieUtils cookieUtils, CookieConfig cookieConfig,
                                      SecurityParameters securityParameters, RefreshTokenService refreshTokenService) {
        this.tokenService = tokenService;
        this.cookieUtils = cookieUtils;
        this.cookieConfig = cookieConfig;
        this.securityParameters = securityParameters;
        this.refreshTokenService = refreshTokenService;
    }

    public CustomOAuth2SuccessHandler(TokenService tokenService, CookieUtils cookieUtils, CookieConfig cookieConfig,
                                      SecurityParameters securityParameters) {
        this(tokenService, cookieUtils, cookieConfig, securityParameters, null);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserAccount userAccount)) {
            throw new ServletException("OAuth2 login succeeded with unsupported principal type: "
                    + (principal == null ? "null" : principal.getClass().getName()));
        }

        SecurityUser securityUser = new SecurityUser(userAccount.getUser());

        TokenPair tokenPair = tokenService.generateTokenPair(securityUser);

        if (refreshTokenService != null) {
            refreshTokenService.createRefreshToken(userAccount.getUser(), tokenPair.getRefreshToken());
        }

        cookieUtils.addCookie(response, "accessToken", tokenPair.getAccessToken(), cookieConfig.getAccessTokenMaxAge());
        cookieUtils.addCookie(response, "refreshToken", tokenPair.getRefreshToken(), cookieConfig.getRefreshTokenMaxAge());

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
