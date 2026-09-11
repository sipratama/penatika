package io.github.sipratama.penatika.identity.application.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ResolvedParticipantSession(
        UUID participantSessionId,
        UUID classroomSessionId,
        String participantRole,
        UUID teacherAccountId,
        UUID teacherBrowserSessionId,
        Instant createdAt,
        Instant expiresAt) {

    public ResolvedParticipantSession {
        Objects.requireNonNull(participantSessionId, "participantSessionId must not be null");
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantRole, "participantRole must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public Optional<UUID> teacherAccountIdOptional() {
        return Optional.ofNullable(teacherAccountId);
    }

    public Optional<UUID> teacherBrowserSessionIdOptional() {
        return Optional.ofNullable(teacherBrowserSessionId);
    }

    @Override
    public String toString() {
        return "ResolvedParticipantSession[REDACTED]";
    }
}
