package io.github.sipratama.penatika.identity.domain;

import java.time.Instant;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

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

    public static final Duration ABSOLUTE_LIFETIME = Duration.ofHours(8);
    private static final Pattern SHA_256_VERIFIER = Pattern.compile("^[0-9a-f]{64}$");

    public ParticipantSession {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantRole, "participantRole must not be null");
        Objects.requireNonNull(credentialVerifier, "credentialVerifier must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (!SHA_256_VERIFIER.matcher(credentialVerifier).matches()) {
            throw new IllegalArgumentException("credentialVerifier must be lowercase SHA-256 hex");
        }
        if (!expiresAt.equals(createdAt.plus(ABSOLUTE_LIFETIME))) {
            throw new IllegalArgumentException("ParticipantSession must expire exactly eight hours after creation");
        }
        boolean controller = participantRole == ParticipantRole.TEACHER_CONTROLLER;
        boolean hasTeacherContext = teacherAccountId != null && teacherBrowserSessionId != null;
        if (controller != hasTeacherContext) {
            throw new IllegalArgumentException(
                    "TEACHER_CONTROLLER requires teacher/browser-session context; "
                            + "CLASSROOM_DISPLAY must not carry teacher authority");
        }
        if (revokedAt != null && revokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("revokedAt must not precede createdAt");
        }
    }

    public static ParticipantSession controller(
            ParticipantSessionId id,
            ClassroomSessionReference classroomSessionId,
            String credentialVerifier,
            TeacherAccountId teacherAccountId,
            TeacherBrowserSessionId teacherBrowserSessionId,
            Instant createdAt) {
        return new ParticipantSession(
                id,
                classroomSessionId,
                ParticipantRole.TEACHER_CONTROLLER,
                credentialVerifier,
                teacherAccountId,
                teacherBrowserSessionId,
                createdAt,
                createdAt.plus(ABSOLUTE_LIFETIME),
                null);
    }

    public static ParticipantSession display(
            ParticipantSessionId id,
            ClassroomSessionReference classroomSessionId,
            String credentialVerifier,
            Instant createdAt) {
        return new ParticipantSession(
                id,
                classroomSessionId,
                ParticipantRole.CLASSROOM_DISPLAY,
                credentialVerifier,
                null,
                null,
                createdAt,
                createdAt.plus(ABSOLUTE_LIFETIME),
                null);
    }

    public Optional<Instant> revokedAtOptional() {
        return Optional.ofNullable(revokedAt);
    }

    public boolean isActive() {
        return revokedAt == null;
    }

    public boolean isUsableAt(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return isActive() && now.isBefore(expiresAt);
    }

    @Override
    public String toString() {
        return "ParticipantSession[REDACTED]";
    }
}
