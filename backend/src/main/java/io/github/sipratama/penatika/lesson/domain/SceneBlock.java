package io.github.sipratama.penatika.lesson.domain;

import java.util.Objects;

public record SceneBlock(
        SceneBlockId id,
        LessonSceneId lessonSceneId,
        long position,
        SceneBlockType type,
        String plainText) {

    public SceneBlock {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(lessonSceneId, "lessonSceneId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(plainText, "plainText must not be null");
        if (position < 0) {
            throw new IllegalArgumentException("position must not be negative");
        }
        if (plainText.isEmpty() || plainText.length() > 16384) {
            throw new IllegalArgumentException("plainText length must be between 1 and 16384");
        }
    }
}
