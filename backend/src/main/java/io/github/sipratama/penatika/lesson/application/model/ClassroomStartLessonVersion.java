package io.github.sipratama.penatika.lesson.application.model;

import java.util.Objects;
import java.util.UUID;

public record ClassroomStartLessonVersion(String externalId, UUID internalId) {

    public ClassroomStartLessonVersion {
        Objects.requireNonNull(externalId, "externalId must not be null");
        Objects.requireNonNull(internalId, "internalId must not be null");
    }
}
