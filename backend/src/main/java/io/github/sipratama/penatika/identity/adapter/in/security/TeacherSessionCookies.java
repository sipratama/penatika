package io.github.sipratama.penatika.identity.adapter.in.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class TeacherSessionCookies {

    public static final String SESSION_COOKIE_NAME = "__Host-penatika-session";
    public static final String CSRF_RECOVERY_COOKIE_NAME = "__Host-penatika-csrf";

    public Optional<RawSecurityToken> readSessionCredential(HttpServletRequest request) {
        return readRawToken(request, SESSION_COOKIE_NAME);
    }

    public Optional<RawSecurityToken> readCsrfRecoveryToken(HttpServletRequest request) {
        return readRawToken(request, CSRF_RECOVERY_COOKIE_NAME);
    }

    public void setSessionCookies(
            HttpServletResponse response,
            RawSecurityToken sessionCredential,
            RawSecurityToken csrfToken,
            Instant absoluteExpiresAt,
            Instant now) {
        Duration remaining = Duration.between(now, absoluteExpiresAt);
        addCookie(response, SESSION_COOKIE_NAME, sessionCredential, remaining);
        addCookie(response, CSRF_RECOVERY_COOKIE_NAME, csrfToken, remaining);
    }

    public void setCsrfRecoveryCookie(
            HttpServletResponse response,
            RawSecurityToken csrfToken,
            Instant absoluteExpiresAt,
            Instant now) {
        addCookie(response, CSRF_RECOVERY_COOKIE_NAME, csrfToken, Duration.between(now, absoluteExpiresAt));
    }

    public void clear(HttpServletResponse response) {
        clearCookie(response, SESSION_COOKIE_NAME);
        clearCookie(response, CSRF_RECOVERY_COOKIE_NAME);
    }

    private Optional<RawSecurityToken> readRawToken(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .flatMap(cookie -> parse(cookie.getValue()));
    }

    private Optional<RawSecurityToken> parse(String value) {
        try {
            return Optional.of(RawSecurityToken.fromEncoded(value));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            RawSecurityToken token,
            Duration remainingAbsoluteLifetime) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, token.expose())
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict");
        if (!remainingAbsoluteLifetime.isNegative() && !remainingAbsoluteLifetime.isZero()) {
            builder.maxAge(remainingAbsoluteLifetime);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private void clearCookie(HttpServletResponse response, String name) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, "")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build()
                .toString());
    }
}
