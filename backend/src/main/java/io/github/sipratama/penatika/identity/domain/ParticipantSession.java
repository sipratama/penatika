package io.github.sipratama.penatika.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record ParticipantSession(
        ParticipantSessionId id,
        ClassroomSessionReference classroomSessionId,
        ParticipantRole participantRole,
        String credentialVerifier,
        TeacherAccountId teacherAccountId,
        TeacherBrowserSessionId teacherBrowserSessionId,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt) {

    public ParticipantSession {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantRole, "participantRole must not be null");
        Objects.requireNonNull(credentialVerifier, "credentialVerifier must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        boolean controller = participantRole == ParticipantRole.TEACHER_CONTROLLER;
        boolean hasTeacherContext = teacherAccountId != null && teacherBrowserSessionId != null;
        if (controller != hasTeacherContext) {
            throw new IllegalArgumentException(
                    "TEACHER_CONTROLLER requires teacher/browser-session context; "
                            + "CLASSROOM_DISPLAY must not carry teacher authority");
        }
    }

    public Optional<Instant> revokedAtOptional() {
        return Optional.ofNullable(revokedAt);
    }

    public boolean isActive() {
        return revokedAt == null;
    }
}
