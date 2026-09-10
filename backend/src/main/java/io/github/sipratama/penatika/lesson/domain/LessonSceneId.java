package io.github.sipratama.penatika.lesson.domain;

import java.util.Objects;
import java.util.UUID;

public record LessonSceneId(UUID value) {

    public LessonSceneId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
