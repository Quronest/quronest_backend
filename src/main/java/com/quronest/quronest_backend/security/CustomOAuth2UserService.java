package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.security.dto.GithubEmailsDto;
import com.quronest.quronest_backend.security.dto.GithubUserDto;
import com.quronest.quronest_backend.service.UserService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final QuronestConfig quronestConfig;

    public CustomOAuth2UserService(UserService userService, QuronestConfig quronestConfig) {
        this.userService = userService;
        this.quronestConfig = quronestConfig;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = loadOAuth2User(userRequest);

        User dbUser = null;
        try {
            dbUser = userService.getOrCreateOAuth2User(oAuth2User.getAttribute("email"),
                                                       oAuth2User.getAttribute("name"),
                                                       oAuth2User.getAttribute("avatar_url"));
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                    "Could not find or create user account with the email id: " + oAuth2User.getAttribute("email"));
        }
        return new CustomOAuth2User(oAuth2User, dbUser);
    }

    public OAuth2User loadOAuth2User(OAuth2UserRequest userRequest) {
        String clientRegistrationId = userRequest.getClientRegistration().getRegistrationId();
        if (clientRegistrationId.equals("github")) {
            return loadGithubUser(userRequest);
        } else {
            return super.loadUser(userRequest);
        }
    }

    private OAuth2User loadGithubUser(OAuth2UserRequest userRequest) {
        final String MISSING_USER_INFO_URI_ERROR_CODE = "missing_user_info_uri";
        Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new OAuth2UserRequestEntityConverter();
        final ParameterizedTypeReference<List<GithubEmailsDto>> EMAIL_RESPONSE_TYPE =
                new ParameterizedTypeReference<List<GithubEmailsDto>>() {
                };
        final ParameterizedTypeReference<GithubUserDto> USER_RESPONSE_TYPE =
                new ParameterizedTypeReference<GithubUserDto>() {
                };

        RestTemplate restTemplate = new RestTemplate();

        Assert.notNull(userRequest, "userRequest cannot be null");
        if (!StringUtils
                .hasText(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri())) {
            OAuth2Error oauth2Error = new OAuth2Error(MISSING_USER_INFO_URI_ERROR_CODE,
                                                      "Missing required UserInfo Uri in UserInfoEndpoint for Client " +
                                                              "Registration: "
                                                              + userRequest.getClientRegistration().getRegistrationId(),
                                                      null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }


        // get user data "/user"
        RequestEntity<?> request = requestEntityConverter.convert(userRequest);
        final ResponseEntity<GithubUserDto> response = restTemplate.exchange(request,
                                                                             USER_RESPONSE_TYPE);

        // get user emails "/user/emails"
        URI emailsUri = URI.create(quronestConfig.getGithub_useremail_uri());
        RequestEntity<?> emailRequest = new RequestEntity<>(
                request.getBody(),
                request.getHeaders(),
                request.getMethod(),
                emailsUri
        );
        final ResponseEntity<List<GithubEmailsDto>> emailResponse = restTemplate.exchange(emailRequest,
                                                                                          EMAIL_RESPONSE_TYPE);

        GithubUserDto userResponseBody = response.getBody();
        List<GithubEmailsDto> emailResponseBody = emailResponse.getBody();

        Map<String, Object> attributes = new HashMap<>();

        attributes.put("avatar_url", userResponseBody.getAvatar_url());
        attributes.put("name", userResponseBody.getName());

        for (GithubEmailsDto githubEmailsDto : emailResponseBody) {
            if (githubEmailsDto.getPrimary()) {
                attributes.put("email", githubEmailsDto.getEmail());
            }
        }
        attributes.put("allemails", emailResponseBody);

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new OAuth2UserAuthority(attributes));
        OAuth2AccessToken token = userRequest.getAccessToken();
        for (String authority : token.getScopes()) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
        }
        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}
