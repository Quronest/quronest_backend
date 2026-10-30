package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.config.CookieConfig;
import com.quronest.quronest_backend.dto.TokenPair;
import com.quronest.quronest_backend.model.table.RefreshToken;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.RefreshTokenRepository;
import com.quronest.quronest_backend.security.CookieUtils;
import com.quronest.quronest_backend.security.SecurityUser;
import com.quronest.quronest_backend.security.TokenService;
import com.quronest.quronest_backend.utils.CryptoUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final CookieUtils cookieUtils;
    private final CookieConfig cookieConfig;


    @Transactional
    public RefreshToken createRefreshToken(User user, String rawRefreshToken) {
        String hashedToken = CryptoUtils.hash(rawRefreshToken);
        LocalDateTime expiryDate = tokenService.calculateRefreshTokenExpiryDate();
        RefreshToken refreshToken = new RefreshToken(user, hashedToken, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return cookieUtils.getRefreshToken(request);
    }

    @Transactional
    public Optional<Authentication> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshTokenOpt = getRefreshToken(request);
        if (refreshTokenOpt.isEmpty() || refreshTokenOpt.get().isBlank()) {
            handleInvalidToken(response);
            return Optional.empty();
        }

        String rawRefreshToken = refreshTokenOpt.get();
        String hashedToken = CryptoUtils.hash(rawRefreshToken);

        Optional<RefreshToken> tokenEntityOpt = refreshTokenRepository.findByToken(hashedToken);
        if (tokenEntityOpt.isEmpty()) {
            handleInvalidToken(response);
            return Optional.empty();
        }

        RefreshToken tokenEntity = tokenEntityOpt.get();

        // Verify expiry in the database
        if (tokenEntity.isExpired()) {
            refreshTokenRepository.delete(tokenEntity);
            handleInvalidToken(response);
            return Optional.empty();
        }

        User user = tokenEntity.getUser();
        if (user == null || user.isBlacklisted()) {
            refreshTokenRepository.delete(tokenEntity);
            handleInvalidToken(response);
            return Optional.empty();
        }

        UserDetails userDetails = new SecurityUser(user);

        // Regenerate token pairs
        TokenPair newPair = tokenService.generateTokenPair(userDetails);

        // Rotate token in database row
        String newHashedToken = CryptoUtils.hash(newPair.getRefreshToken());
        LocalDateTime newExpiryDate = tokenService.calculateRefreshTokenExpiryDate();
        tokenEntity.setToken(newHashedToken);
        tokenEntity.setExpiryDate(newExpiryDate);
        refreshTokenRepository.save(tokenEntity);

        // Set rotated cookies on response
        cookieUtils.addCookie(response, "accessToken", newPair.getAccessToken(), cookieConfig.getAccessTokenMaxAge());
        cookieUtils.addCookie(response, "refreshToken", newPair.getRefreshToken(),
                              cookieConfig.getRefreshTokenMaxAge());

        // Create authentication
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return Optional.of(authToken);
    }

    @Transactional
    public void revokeRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        getRefreshToken(request).ifPresent(rawToken -> {
            String hashed = CryptoUtils.hash(rawToken);
            refreshTokenRepository.findByToken(hashed).ifPresent(refreshTokenRepository::delete);
        });
        handleInvalidToken(response);
    }

    public void handleInvalidToken(HttpServletResponse response) {
        cookieUtils.clearCookie(response, "accessToken");
        cookieUtils.clearCookie(response, "refreshToken");
        SecurityContextHolder.clearContext();
    }
}
