package io.github.sipratama.penatika.lesson.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record LessonVersion(
        LessonVersionId id,
        LessonId lessonId,
        LessonVersionReadiness readiness,
        Instant createdAt,
        List<LessonScene> scenes) {

    public LessonVersion {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(lessonId, "lessonId must not be null");
        Objects.requireNonNull(readiness, "readiness must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        scenes = List.copyOf(Objects.requireNonNull(scenes, "scenes must not be null"));
    }

    public boolean isClassroomReady() {
        return readiness == LessonVersionReadiness.CLASSROOM_READY;
    }
}
