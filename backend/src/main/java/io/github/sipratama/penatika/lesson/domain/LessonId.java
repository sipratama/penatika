package io.github.sipratama.penatika.lesson.domain;

import java.util.Objects;
import java.util.UUID;

public record LessonId(UUID value) {

    public LessonId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
