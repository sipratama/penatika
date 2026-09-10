package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;

public record StartedClassroomSession(
        String classroomSessionId,
        String lessonVersionId,
        long revision) {

    public StartedClassroomSession {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(lessonVersionId, "lessonVersionId must not be null");
    }
}
