package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;
import java.util.UUID;

public record AuthorizedController(
        UUID teacherAccountId,
        UUID teacherBrowserSessionId,
        UUID participantSessionId) {

    public AuthorizedController {
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(teacherBrowserSessionId, "teacherBrowserSessionId must not be null");
        Objects.requireNonNull(participantSessionId, "participantSessionId must not be null");
    }
}
