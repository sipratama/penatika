package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;

import io.github.sipratama.penatika.classroom.domain.Revision;

public record ClassroomDisplayProjection(
        String schemaVersion,
        String classroomSessionId,
        long revision,
        ClassroomDisplayScene scene) {

    public ClassroomDisplayProjection {
        Objects.requireNonNull(schemaVersion, "schemaVersion must not be null");
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(scene, "scene must not be null");
        int idLength = classroomSessionId.codePointCount(0, classroomSessionId.length());
        if (!"1.0".equals(schemaVersion)
                || idLength < 1
                || idLength > 128
                || classroomSessionId.codePoints().anyMatch(Character::isWhitespace)
                || revision < 0
                || revision > Revision.MAX_VALUE) {
            throw new IllegalArgumentException("invalid Display projection");
        }
    }
}
