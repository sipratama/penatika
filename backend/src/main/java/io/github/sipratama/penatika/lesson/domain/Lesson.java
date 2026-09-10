package io.github.sipratama.penatika.lesson.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Lesson(LessonId id, UUID teacherAccountId, Instant createdAt) {

    public Lesson {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
