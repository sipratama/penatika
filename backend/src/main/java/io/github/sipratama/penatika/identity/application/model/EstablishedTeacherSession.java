package io.github.sipratama.penatika.identity.application.model;

import java.time.Instant;
import java.util.Objects;

public final class EstablishedTeacherSession {

    private final RawSecurityToken sessionCredential;
    private final RawSecurityToken csrfToken;
    private final Instant absoluteExpiresAt;

    public EstablishedTeacherSession(
            RawSecurityToken sessionCredential,
            RawSecurityToken csrfToken,
            Instant absoluteExpiresAt) {
        this.sessionCredential = Objects.requireNonNull(sessionCredential, "sessionCredential must not be null");
        this.csrfToken = Objects.requireNonNull(csrfToken, "csrfToken must not be null");
        this.absoluteExpiresAt = Objects.requireNonNull(absoluteExpiresAt, "absoluteExpiresAt must not be null");
    }

    public RawSecurityToken sessionCredential() {
        return sessionCredential;
    }

    public RawSecurityToken csrfToken() {
        return csrfToken;
    }

    public Instant absoluteExpiresAt() {
        return absoluteExpiresAt;
    }

    @Override
    public String toString() {
        return "EstablishedTeacherSession[REDACTED]";
    }
}
