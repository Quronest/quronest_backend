package com.quronest.quronest_backend.config;

import com.quronest.quronest_backend.config.OAuthClient.OAuth2LoginConfig;
import com.quronest.quronest_backend.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomLogoutHandler customLogoutHandler;
    private final OAuth2LoginConfig oAuth2LoginConfig;
    private final SecurityParameters securityParameters;
    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter, CustomLogoutHandler customLogoutHandler,
                          OAuth2LoginConfig oAuth2LoginConfig, SecurityParameters securityParameters,
                          CustomOidcUserService customOidcUserService,
                          CustomOAuth2UserService customOAuth2UserService,
                          CustomOAuth2SuccessHandler customOAuth2SuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customLogoutHandler = customLogoutHandler;
        this.oAuth2LoginConfig = oAuth2LoginConfig;
        this.securityParameters = securityParameters;
        this.customOidcUserService = customOidcUserService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
    }

    @Bean
    @Order(1)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                                           session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permit public urls
                        .requestMatchers(Urls.ALL_PUBLIC_URLS).permitAll()
                        // Rest should be authenticated
                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider())

                // userDetails service
                .userDetailsService(userDetailsService)

                // Oauth2 Login (oauth2 client)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage(securityParameters.getLoginpage())
                        .authorizationEndpoint(a -> a.authorizationRequestResolver(
                                oAuth2LoginConfig.oAuth2AuthorizationRequestResolver()))
                        .defaultSuccessUrl(securityParameters.getHomepage(), false)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService)
                                .userService(customOAuth2UserService))
                        .successHandler(customOAuth2SuccessHandler)
                )


                // jwt filter chain
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Forbidden if not authenticated
                .exceptionHandling(customizer -> customizer.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // logout
                .logout(logout -> logout
                        .logoutUrl(Urls.PUBLIC_AUTH_CONTROLLER + "/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                        .invalidateHttpSession(true)
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
