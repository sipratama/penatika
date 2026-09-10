package io.github.sipratama.penatika.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record TeacherBrowserSession(
        TeacherBrowserSessionId id,
        TeacherAccountId teacherAccountId,
        String credentialVerifier,
        String csrfVerifier,
        Instant createdAt,
        Instant lastActiveAt,
        Instant idleExpiresAt,
        Instant absoluteExpiresAt,
        Instant revokedAt) {

    public TeacherBrowserSession {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(credentialVerifier, "credentialVerifier must not be null");
        Objects.requireNonNull(csrfVerifier, "csrfVerifier must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(lastActiveAt, "lastActiveAt must not be null");
        Objects.requireNonNull(idleExpiresAt, "idleExpiresAt must not be null");
        Objects.requireNonNull(absoluteExpiresAt, "absoluteExpiresAt must not be null");
    }

    public Optional<Instant> revokedAtOptional() {
        return Optional.ofNullable(revokedAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
