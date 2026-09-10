package io.github.sipratama.penatika.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record TeacherAccountId(UUID value) {

    public TeacherAccountId {
        Objects.requireNonNull(value, "value must not be null");
    }
}
