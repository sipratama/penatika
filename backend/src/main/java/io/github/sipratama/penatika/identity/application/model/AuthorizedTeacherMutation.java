package io.github.sipratama.penatika.identity.application.model;

import java.util.Objects;
import java.util.UUID;

public record AuthorizedTeacherMutation(UUID teacherAccountId) {

    public AuthorizedTeacherMutation {
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
    }
}
