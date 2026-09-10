package io.github.sipratama.penatika.identity;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;

import io.github.sipratama.penatika.identity.adapter.in.http.TeacherSessionAuthenticationEntryPoint;
import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionAuthenticationFilter;
import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionCookies;
import io.github.sipratama.penatika.identity.adapter.out.oidc.EphemeralOAuth2AuthorizedClientRepository;
import io.github.sipratama.penatika.identity.adapter.out.oidc.PenatikaOidcAuthenticationSuccessHandler;
import io.github.sipratama.penatika.identity.application.port.in.AuthenticateTeacherBrowserSessionUseCase;
import io.github.sipratama.penatika.identity.application.port.in.EstablishTeacherBrowserSessionUseCase;

@Configuration(proxyBeanMethods = false)
public class IdentitySecurityConfiguration {

    @Bean
    TeacherSessionCookies teacherSessionCookies() {
        return new TeacherSessionCookies();
    }

    @Bean
    @ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
    TeacherSessionAuthenticationFilter teacherSessionAuthenticationFilter(
            AuthenticateTeacherBrowserSessionUseCase authenticateSession,
            TeacherSessionCookies cookies) {
        return new TeacherSessionAuthenticationFilter(authenticateSession, cookies);
    }

    @Bean
    TeacherSessionAuthenticationEntryPoint teacherSessionAuthenticationEntryPoint(
            TeacherSessionCookies cookies) {
        return new TeacherSessionAuthenticationEntryPoint(cookies);
    }

    @Bean
    @Order(1)
    SecurityFilterChain oidcAuthenticationChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationProvider,
            ObjectProvider<EstablishTeacherBrowserSessionUseCase> establishSessionProvider,
            ObjectProvider<java.time.Clock> clockProvider,
            TeacherSessionCookies cookies) throws Exception {
        ClientRegistrationRepository clientRegistrations = clientRegistrationProvider.getIfAvailable();
        EstablishTeacherBrowserSessionUseCase establishSession = establishSessionProvider.getIfAvailable();
        if (clientRegistrations == null || establishSession == null) {
            http.securityMatcher("/oauth2/**", "/login/oauth2/**")
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll())
                    .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                    .securityContext(context -> context.securityContextRepository(
                            new NullSecurityContextRepository()))
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
        DefaultOAuth2AuthorizationRequestResolver authorizationRequests =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrations);
        authorizationRequests.setAuthorizationRequestCustomizer(
                OAuth2AuthorizationRequestCustomizers.withPkce());
        PenatikaOidcAuthenticationSuccessHandler successHandler =
                new PenatikaOidcAuthenticationSuccessHandler(
                        establishSession,
                        cookies,
                        clockProvider.getIfAvailable(java.time.Clock::systemUTC));

        http.securityMatcher("/oauth2/**", "/login/oauth2/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .securityContext(context -> context.securityContextRepository(
                        new NullSecurityContextRepository()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .clientRegistrationRepository(clientRegistrations)
                        .authorizedClientRepository(new EphemeralOAuth2AuthorizedClientRepository())
                        .securityContextRepository(new NullSecurityContextRepository())
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(authorizationRequests))
                        .successHandler(successHandler));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain penatikaApiChain(
            HttpSecurity http,
            ObjectProvider<TeacherSessionAuthenticationFilter> authenticationFilter,
            TeacherSessionAuthenticationEntryPoint entryPoint) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/teacher-session").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/classroom-sessions").authenticated()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .securityContext(context -> context.securityContextRepository(
                        new NullSecurityContextRepository()))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);
        authenticationFilter.ifAvailable(filter -> http.addFilterBefore(filter, AnonymousAuthenticationFilter.class));
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain fallbackDenyChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll())
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .csrf(Customizer.withDefaults());
        return http.build();
    }
}
