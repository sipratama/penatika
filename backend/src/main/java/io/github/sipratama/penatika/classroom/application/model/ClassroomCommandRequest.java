package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public record ClassroomCommandRequest(
        UUID classroomSessionId,
        String commandId,
        long expectedRevision,
        UUID teacherAccountId,
        UUID teacherBrowserSessionId,
        Optional<RawSecurityToken> participantCredential) {

    public ClassroomCommandRequest {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(commandId, "commandId must not be null");
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(teacherBrowserSessionId, "teacherBrowserSessionId must not be null");
        Objects.requireNonNull(participantCredential, "participantCredential must not be null");
    }

    @Override
    public String toString() {
        return "ClassroomCommandRequest[REDACTED]";
    }
}
