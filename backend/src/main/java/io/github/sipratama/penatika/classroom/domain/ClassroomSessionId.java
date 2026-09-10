package io.github.sipratama.penatika.classroom.domain;

import java.util.Objects;
import java.util.UUID;

public record ClassroomSessionId(UUID value) {

    public ClassroomSessionId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
