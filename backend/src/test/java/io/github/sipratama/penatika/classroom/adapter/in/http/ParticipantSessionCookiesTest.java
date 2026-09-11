package io.github.sipratama.penatika.classroom.adapter.in.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import jakarta.servlet.http.Cookie;

class ParticipantSessionCookiesTest {

    private final ParticipantSessionCookies cookies = new ParticipantSessionCookies();

    @Test
    void readsOnlyAWellFormedParticipantCredentialWithoutExposingAnotherCookie() {
        RawSecurityToken credential = RawSecurityToken.fromEncoded("p".repeat(43));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("other", "ignored"),
                new Cookie(ParticipantSessionCookies.COOKIE_NAME, credential.expose()));

        assertThat(cookies.readParticipantCredential(request))
                .get()
                .extracting(RawSecurityToken::expose)
                .isEqualTo(credential.expose());
    }

    @Test
    void missingOrMalformedParticipantCredentialFailsClosed() {
        assertThat(cookies.readParticipantCredential(new MockHttpServletRequest())).isEmpty();
        MockHttpServletRequest malformed = new MockHttpServletRequest();
        malformed.setCookies(new Cookie(ParticipantSessionCookies.COOKIE_NAME, "malformed"));
        assertThat(cookies.readParticipantCredential(malformed)).isEmpty();
    }
}
