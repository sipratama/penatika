package io.github.sipratama.penatika.lesson.domain;

import java.util.Objects;
import java.util.UUID;

public record LessonVersionId(UUID value) {

    public LessonVersionId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
