package io.github.sipratama.penatika.lesson.domain;

import java.util.Objects;
import java.util.UUID;

public record SceneBlockId(UUID value) {

    public SceneBlockId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
