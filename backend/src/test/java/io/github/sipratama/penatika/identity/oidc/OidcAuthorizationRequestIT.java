package io.github.sipratama.penatika.identity.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import io.github.sipratama.penatika.identity.application.model.EstablishedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.ExternalTeacherIdentity;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.in.EstablishTeacherBrowserSessionUseCase;

@SpringBootTest
@Import(OidcAuthorizationRequestIT.OidcTestConfiguration.class)
class OidcAuthorizationRequestIT {

    @Autowired private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void authorizationCodeRequestIncludesStateNonceAndPkceWithoutOfflineAccess() throws Exception {
        String redirect = mockMvc.perform(get("/oauth2/authorization/test"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        UriComponents authorization = UriComponentsBuilder.fromUriString(redirect).build();
        assertThat(authorization.getScheme()).isEqualTo("https");
        assertThat(authorization.getHost()).isEqualTo("identity.example.test");
        assertThat(authorization.getPath()).isEqualTo("/authorize");
        assertThat(authorization.getQueryParams().getFirst("response_type")).isEqualTo("code");
        assertThat(authorization.getQueryParams().getFirst("state")).isNotBlank();
        assertThat(authorization.getQueryParams().getFirst("nonce")).isNotBlank();
        assertThat(authorization.getQueryParams().getFirst("code_challenge"))
                .isNotBlank()
                .hasSize(43);
        assertThat(authorization.getQueryParams().getFirst("code_challenge_method"))
                .isEqualTo("S256");
        assertThat(authorization.getQueryParams().getFirst("scope")).isEqualTo("openid");
        assertThat(authorization.getQueryParams().getFirst("scope")).doesNotContain("offline_access");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class OidcTestConfiguration {

        @Bean
        Clock oidcTestClock() {
            return Clock.fixed(Instant.parse("2026-09-10T10:00:00Z"), ZoneOffset.UTC);
        }

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration registration = ClientRegistration.withRegistrationId("test")
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid")
                    .authorizationUri("https://identity.example.test/authorize")
                    .tokenUri("https://identity.example.test/token")
                    .jwkSetUri("https://identity.example.test/jwks")
                    .userNameAttributeName("sub")
                    .clientName("Test OIDC")
                    .build();
            return new InMemoryClientRegistrationRepository(registration);
        }

        @Bean
        EstablishTeacherBrowserSessionUseCase establishTeacherBrowserSessionUseCase() {
            return new EstablishTeacherBrowserSessionUseCase() {
                @Override
                public EstablishedTeacherSession establish(
                        ExternalTeacherIdentity externalIdentity,
                        Optional<RawSecurityToken> currentBrowserCredential) {
                    throw new AssertionError("Authorization initiation must not establish a session");
                }
            };
        }
    }
}
