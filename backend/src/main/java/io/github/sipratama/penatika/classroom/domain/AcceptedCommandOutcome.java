package io.github.sipratama.penatika.classroom.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AcceptedCommandOutcome(
        AcceptedCommandOutcomeId id,
        ClassroomSessionId classroomSessionId,
        String commandId,
        Revision expectedRevision,
        CommandType commandType,
        ClassroomAction action,
        UUID teacherAccountId,
        UUID teacherBrowserSessionId,
        UUID controllerParticipantSessionId,
        Revision resultingRevision,
        Instant acceptedAt) {

    public AcceptedCommandOutcome {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(commandId, "commandId must not be null");
        Objects.requireNonNull(expectedRevision, "expectedRevision must not be null");
        Objects.requireNonNull(commandType, "commandType must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(teacherBrowserSessionId, "teacherBrowserSessionId must not be null");
        Objects.requireNonNull(controllerParticipantSessionId, "controllerParticipantSessionId must not be null");
        Objects.requireNonNull(resultingRevision, "resultingRevision must not be null");
        Objects.requireNonNull(acceptedAt, "acceptedAt must not be null");
        if (commandId.isBlank() || commandId.length() > 128) {
            throw new IllegalArgumentException("commandId length must be between 1 and 128");
        }
        if (resultingRevision.value() <= expectedRevision.value()) {
            throw new IllegalArgumentException("resultingRevision must advance expectedRevision");
        }
    }
}
