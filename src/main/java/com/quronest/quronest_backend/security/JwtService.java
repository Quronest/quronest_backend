package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.Constants;
import com.quronest.quronest_backend.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
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

    // HMAC tokens

    public String extractUsername(String token) {
        return extractClaim(token, jwtConfig.getAccessTokenSecret(), Claims::getSubject);
    }

    public <T> T extractClaim(String token, String secretKey, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails user) {
        return buildHMACToken(new HashMap<>(), user, jwtConfig.getAccessTokenSecret(),
                              jwtConfig.getAccessTokenExpiry());
    }

    public String generateRefreshToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return buildHMACToken(claims, user, jwtConfig.getRefreshTokenSecret(), jwtConfig.getRefreshTokenExpiry());
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        return isTokenValid(token, jwtConfig.getAccessTokenSecret(), user);
    }

    private String buildHMACToken(Map<String, Object> extraClaims, UserDetails user, String secretKey,
                                  long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getHMACSignInKey(secretKey))
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
                .verifyWith(getHMACSignInKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getHMACSignInKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // RSA Tokens

    public String generateLLMServiceToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "service");
        claims.put("scope", "llm");
        return buildRSAToken(claims, Constants.SERVICE_LLM, jwtConfig.getServiceKyExpiration());
    }

    private String buildRSAToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    // load rsa keys
    private PrivateKey getPrivateKey() {
        try {
            String key = new String(Files.readAllBytes(Paths.get(jwtConfig.getPrivateKeyPath())));
            key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error loading private key", e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            String key = new String(Files.readAllBytes(Paths.get(jwtConfig.getPublicKeyPath())));
            key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error loading public key", e);
        }
    }
}
