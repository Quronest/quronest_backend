package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;
    private final CookieUtils cookieUtils;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = cookieUtils.getCookieValue(request, "accessToken").orElse(null);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = tokenService.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (tokenService.isAccessTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (ExpiredJwtException ex) {
                handleExpiredToken(request, response);
            } catch (JwtException ex) {
                handleInvalidToken(response);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleExpiredToken(HttpServletRequest request, HttpServletResponse response) {
        Optional<Authentication> authenticationOpt = refreshTokenService.refreshTokens(request, response);
        if (authenticationOpt.isPresent()) {
            SecurityContextHolder.getContext().setAuthentication(authenticationOpt.get());
        } else {
            handleInvalidToken(response);
        }
    }

    private void handleInvalidToken(HttpServletResponse response) {
        refreshTokenService.handleInvalidToken(response);
    }
}
