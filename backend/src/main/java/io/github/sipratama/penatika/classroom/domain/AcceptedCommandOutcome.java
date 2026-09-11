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
        int commandIdLength = commandId.codePointCount(0, commandId.length());
        if (commandIdLength < 1
                || commandIdLength > 128
                || commandId.codePoints().anyMatch(AcceptedCommandOutcome::isWhitespace)) {
            throw new IllegalArgumentException("commandId must contain 1 to 128 non-whitespace code points");
        }
        if (resultingRevision.value() <= expectedRevision.value()) {
            throw new IllegalArgumentException("resultingRevision must advance expectedRevision");
        }
    }

    public boolean isEquivalentTo(
            ClassroomSessionId requestedClassroomSessionId,
            String requestedCommandId,
            Revision requestedExpectedRevision,
            CommandType requestedCommandType,
            ClassroomAction requestedAction) {
        return classroomSessionId.equals(requestedClassroomSessionId)
                && commandId.equals(requestedCommandId)
                && expectedRevision.equals(requestedExpectedRevision)
                && commandType == requestedCommandType
                && action == requestedAction;
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }
}
