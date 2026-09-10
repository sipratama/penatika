package io.github.sipratama.penatika.identity.application.model;

import java.time.Instant;
import java.util.Objects;

public final class TeacherSessionBootstrap {

    private final RawSecurityToken csrfToken;
    private final Instant absoluteExpiresAt;
    private final boolean recoveryCookieReplaced;

    public TeacherSessionBootstrap(
            RawSecurityToken csrfToken,
            Instant absoluteExpiresAt,
            boolean recoveryCookieReplaced) {
        this.csrfToken = Objects.requireNonNull(csrfToken, "csrfToken must not be null");
        this.absoluteExpiresAt = Objects.requireNonNull(absoluteExpiresAt, "absoluteExpiresAt must not be null");
        this.recoveryCookieReplaced = recoveryCookieReplaced;
    }

    public RawSecurityToken csrfToken() {
        return csrfToken;
    }

    public Instant absoluteExpiresAt() {
        return absoluteExpiresAt;
    }

    public boolean recoveryCookieReplaced() {
        return recoveryCookieReplaced;
    }

    @Override
    public String toString() {
        return "TeacherSessionBootstrap[REDACTED]";
    }
}
