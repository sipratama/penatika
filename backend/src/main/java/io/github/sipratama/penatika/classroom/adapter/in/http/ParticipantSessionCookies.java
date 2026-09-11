package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public final class ParticipantSessionCookies {

    public static final String COOKIE_NAME = "__Host-penatika-participant";

    public Optional<RawSecurityToken> readParticipantCredential(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .flatMap(cookie -> parse(cookie.getValue()));
    }

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

    private static Optional<RawSecurityToken> parse(String value) {
        try {
            return Optional.of(RawSecurityToken.fromEncoded(value));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Optional.empty();
        }
    }
}
