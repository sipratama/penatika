package io.github.sipratama.penatika.classroom.adapter.in.http;

import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandResult;

public record ClassroomCommandResponse(
        String classroomSessionId,
        String commandId,
        long resultingRevision) {

    public ClassroomCommandResponse(ClassroomCommandResult result) {
        this(result.classroomSessionId(), result.commandId(), result.resultingRevision());
    }
}
