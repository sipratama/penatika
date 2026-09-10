package io.github.sipratama.penatika.identity.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionCookies;
import io.github.sipratama.penatika.identity.adapter.out.oidc.PenatikaOidcAuthenticationSuccessHandler;
import io.github.sipratama.penatika.identity.application.TeacherAuthenticationRejectedException;
import io.github.sipratama.penatika.identity.application.model.EstablishedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.ExternalTeacherIdentity;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.in.EstablishTeacherBrowserSessionUseCase;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class PenatikaOidcAuthenticationSuccessHandlerTest {

    private static final Instant NOW = Instant.parse("2026-09-10T10:00:00Z");
    private static final String ISSUER = "https://identity.example.test";
    private static final String SUBJECT = "teacher-subject";
    private static final RawSecurityToken OLD_SESSION = token('O');
    private static final RawSecurityToken NEW_SESSION = token('N');
    private static final RawSecurityToken CSRF_TOKEN = token('C');

    @Mock private EstablishTeacherBrowserSessionUseCase establishSession;

    private PenatikaOidcAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        handler = new PenatikaOidcAuthenticationSuccessHandler(
                establishSession,
                new TeacherSessionCookies(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void mapsValidatedIssuerSubjectSetsSecureCookiesAndClearsTransientAuthority() throws Exception {
        when(establishSession.establish(any(), any())).thenReturn(new EstablishedTeacherSession(
                NEW_SESSION, CSRF_TOKEN, NOW.plusSeconds(8 * 60 * 60)));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, OLD_SESSION.expose()));
        MockHttpSession transientSession = new MockHttpSession();
        request.setSession(transientSession);
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken authentication = oidcAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<ExternalTeacherIdentity> identity =
                ArgumentCaptor.forClass(ExternalTeacherIdentity.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Optional<RawSecurityToken>> oldCredential =
                ArgumentCaptor.forClass(Optional.class);
        verify(establishSession).establish(identity.capture(), oldCredential.capture());
        assertThat(identity.getValue()).isEqualTo(new ExternalTeacherIdentity(ISSUER, SUBJECT));
        assertThat(oldCredential.getValue()).isPresent();
        assertThat(oldCredential.getValue().orElseThrow().expose()).isEqualTo(OLD_SESSION.expose());
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(transientSession.isInvalid()).isTrue();

        List<String> setCookies = response.getHeaders("Set-Cookie");
        assertThat(setCookies).hasSize(2);
        assertSecureHostCookie(setCookies, TeacherSessionCookies.SESSION_COOKIE_NAME, NEW_SESSION);
        assertSecureHostCookie(setCookies, TeacherSessionCookies.CSRF_RECOVERY_COOKIE_NAME, CSRF_TOKEN);
    }

    @Test
    void unknownIdentityFailsWithoutDisclosingIdentityOrSecrets() throws Exception {
        when(establishSession.establish(any(), any())).thenThrow(new TeacherAuthenticationRejectedException());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession transientSession = new MockHttpSession();
        request.setSession(transientSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, oidcAuthentication());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .doesNotContain(ISSUER, SUBJECT, OLD_SESSION.expose(), NEW_SESSION.expose(), CSRF_TOKEN.expose());
        assertThat(transientSession.isInvalid()).isTrue();
        assertThat(response.getHeaders("Set-Cookie"))
                .hasSize(2)
                .allSatisfy(cookie -> assertThat(cookie)
                        .contains("Max-Age=0", "Secure", "HttpOnly", "Path=/", "SameSite=Strict")
                        .doesNotContain("Domain="));
    }

    private static OAuth2AuthenticationToken oidcAuthentication() {
        OidcIdToken idToken = new OidcIdToken(
                "id-token-value",
                NOW.minusSeconds(30),
                NOW.plusSeconds(300),
                Map.of("iss", ISSUER, "sub", SUBJECT));
        DefaultOidcUser user = new DefaultOidcUser(List.of(), idToken);
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), "test");
    }

    private static void assertSecureHostCookie(
            List<String> setCookies, String name, RawSecurityToken expectedValue) {
        assertThat(setCookies)
                .filteredOn(cookie -> cookie.startsWith(name + "="))
                .singleElement()
                .satisfies(cookie -> assertThat(cookie)
                        .contains(
                                name + "=" + expectedValue.expose(),
                                "Max-Age=28800",
                                "Secure",
                                "HttpOnly",
                                "Path=/",
                                "SameSite=Strict")
                        .doesNotContain("Domain="));
    }

    private static RawSecurityToken token(char character) {
        return RawSecurityToken.fromEncoded(String.valueOf(character).repeat(43));
    }
}
