package io.github.sipratama.penatika.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record TeacherBrowserSessionId(UUID value) {

    public TeacherBrowserSessionId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
