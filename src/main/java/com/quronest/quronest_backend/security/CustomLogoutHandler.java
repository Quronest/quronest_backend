package com.quronest.quronest_backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {
    private final CookieUtils cookieUtils;

    public CustomLogoutHandler(CookieUtils cookieUtils) {
        this.cookieUtils = cookieUtils;
    }

    @Override
    public void logout(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                       @Nullable Authentication authentication) {
        // clear cookies
        cookieUtils.clearCookie(response, "accessToken");
        cookieUtils.clearCookie(response, "refreshToken");

        // remove current auth
        SecurityContextHolder.clearContext();
    }
}
