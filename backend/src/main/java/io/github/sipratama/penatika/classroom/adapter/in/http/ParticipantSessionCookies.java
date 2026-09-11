package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import jakarta.servlet.http.HttpServletResponse;

public final class ParticipantSessionCookies {

    public static final String COOKIE_NAME = "__Host-penatika-participant";

    public void setParticipantCookie(
            HttpServletResponse response,
            RawSecurityToken credential,
            Instant expiresAt,
            Instant now) {
        Duration remaining = Duration.between(now, expiresAt);
        if (remaining.isNegative() || remaining.isZero()) {
            throw new IllegalArgumentException("participant cookie requires remaining server-side authority");
        }
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(COOKIE_NAME, credential.expose())
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(remaining)
                .build()
                .toString());
    }
}
