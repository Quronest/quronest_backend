package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.CookieConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtils {

    private final CookieConfig cookieConfig;

    public CookieUtils(CookieConfig cookieConfig) {
        this.cookieConfig = cookieConfig;
    }

    public Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            return Optional.empty();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies).filter(cookie -> name.equals(cookie.getName())).map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank()).map(String::trim).findFirst();
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        response.addHeader("Set-Cookie", buildCookie(name, value, maxAgeSeconds));
    }

    public void clearCookie(HttpServletResponse response, String name) {
        response.addHeader("Set-Cookie", buildCookie(name, "", 0));
    }

    private String buildCookie(String name, String value, int maxAge) {
        return name + "=" + value +
                "; Max-Age=" + maxAge +
                "; Path=/" +
                "; HttpOnly" +
                "; SameSite=Lax" +
                (cookieConfig.isSecure() ? "; Secure" : "");
    }
}
