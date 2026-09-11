package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;

public record ClassroomCommandResult(
        String classroomSessionId,
        String commandId,
        long resultingRevision) {

    public ClassroomCommandResult {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(commandId, "commandId must not be null");
    }
}
