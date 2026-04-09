package com.quronest.quronest_backend.config.OAuthClient;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class OAuth2CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final Logger log = LoggerFactory.getLogger(OAuth2CustomAuthorizationRequestResolver.class);
    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final PathPatternRequestMatcher authorizationRequestMatcher;

    private static final String REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId";

    // Whitelist allowed domains
    private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "localhost",
            "localhost.quronest.com",
            "quronest.com"
    );

    public OAuth2CustomAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            String authorizationRequestBaseUri
    ) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                authorizationRequestBaseUri
        );

        this.authorizationRequestMatcher = PathPatternRequestMatcher.withDefaults()
                .matcher(authorizationRequestBaseUri + "/{" + REGISTRATION_ID_URI_VARIABLE_NAME + "}");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {

        String clientRegistrationId = resolveRegistrationId(request);

        String targetUrl = request.getParameter("targetUrlParameter");

        // Remove unsafe param from request
        CustomAvoidTargetRequestWrapper wrappedRequest =
                new CustomAvoidTargetRequestWrapper(request);

        if (targetUrl != null) {
            try {
                String decodedUrl = URLDecoder.decode(targetUrl, StandardCharsets.UTF_8);
                URL parsedUrl = new URL(decodedUrl);

                // SECURITY: Validate domain
                if (!ALLOWED_DOMAINS.contains(parsedUrl.getHost())) {
                    throw new RuntimeException("Invalid redirect URL");
                }

                // Store in session (clean approach)
                request.getSession().setAttribute("REDIRECT_URI", decodedUrl);

            } catch (Exception e) {
                // log properly in production
                log.info("Invalid target URL: {}", e.getMessage());
            }
        }

        return customizeAuthorizationRequest(
                defaultResolver.resolve(wrappedRequest),
                clientRegistrationId
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return customizeAuthorizationRequest(
                defaultResolver.resolve(request, clientRegistrationId),
                clientRegistrationId
        );
    }

    private String resolveRegistrationId(HttpServletRequest request) {
        if (this.authorizationRequestMatcher.matches(request)) {
            return this.authorizationRequestMatcher.matcher(request)
                    .getVariables()
                    .get(REGISTRATION_ID_URI_VARIABLE_NAME);
        }
        return null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest req,
            String clientRegistrationId
    ) {
        if (req == null) {
            return null;
        }
        return OAuth2AuthorizationRequest.from(req).build();
    }
}