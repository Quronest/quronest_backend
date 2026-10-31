package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;

    public CustomLogoutHandler(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void logout(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                       @Nullable Authentication authentication) {
        refreshTokenService.revokeRefreshToken(request, response);
    }
}
