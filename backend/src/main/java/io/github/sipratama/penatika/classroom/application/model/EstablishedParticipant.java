package io.github.sipratama.penatika.classroom.application.model;

import java.time.Instant;
import java.util.Objects;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public record EstablishedParticipant(
        String classroomSessionId,
        String participantRole,
        RawSecurityToken participantCredential,
        Instant createdAt,
        Instant expiresAt) {

    public EstablishedParticipant {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantRole, "participantRole must not be null");
        Objects.requireNonNull(participantCredential, "participantCredential must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    @Override
    public String toString() {
        return "EstablishedParticipant[REDACTED]";
    }
}
