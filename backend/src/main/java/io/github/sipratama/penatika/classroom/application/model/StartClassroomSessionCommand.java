package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;
import java.util.UUID;

public record StartClassroomSessionCommand(
        UUID teacherAccountId,
        UUID lessonVersionId,
        String externalLessonVersionId) {

    public StartClassroomSessionCommand {
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(lessonVersionId, "lessonVersionId must not be null");
        Objects.requireNonNull(externalLessonVersionId, "externalLessonVersionId must not be null");
    }
}
