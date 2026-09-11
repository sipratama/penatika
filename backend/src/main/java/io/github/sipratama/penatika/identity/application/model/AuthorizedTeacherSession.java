package io.github.sipratama.penatika.identity.application.model;

import java.util.Objects;
import java.util.UUID;

public record AuthorizedTeacherSession(UUID teacherAccountId, UUID teacherBrowserSessionId) {

    public AuthorizedTeacherSession {
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(teacherBrowserSessionId, "teacherBrowserSessionId must not be null");
    }
}
