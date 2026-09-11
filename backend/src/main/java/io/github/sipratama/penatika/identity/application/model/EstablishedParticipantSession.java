package io.github.sipratama.penatika.identity.application.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EstablishedParticipantSession(
        UUID classroomSessionId,
        RawSecurityToken credential,
        Instant createdAt,
        Instant expiresAt) {

    public EstablishedParticipantSession {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(credential, "credential must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    @Override
    public String toString() {
        return "EstablishedParticipantSession[REDACTED]";
    }
}
