package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String extractUsername(String token) {
        return extractClaim(token, jwtConfig.getAccessTokenSecret(), Claims::getSubject);
    }

    public <T> T extractClaim(String token, String secretKey, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails user) {
        return generateToken(new HashMap<>(), user, jwtConfig.getAccessTokenSecret(),
                             jwtConfig.getAccessTokenExpiry());
    }

    public String generateRefreshToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(claims, user, jwtConfig.getRefreshTokenSecret(), jwtConfig.getRefreshTokenExpiry());
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails user, String secretKey, long expiration) {
        return buildToken(extraClaims, user, secretKey, expiration);
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        return isTokenValid(token, jwtConfig.getAccessTokenSecret(), user);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails user, String secretKey, long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(secretKey))
                .compact();
    }

    public boolean isTokenValid(String token, String secretKey, UserDetails user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername())) && !isTokenExpired(token, secretKey);
    }

    private boolean isTokenExpired(String token, String secretKey) {
        return extractExpiration(token, secretKey).before(new Date());
    }

    private Date extractExpiration(String token, String secretKey) {
        return extractClaim(token, secretKey, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token, String secretKey) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
