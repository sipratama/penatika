package io.github.sipratama.penatika.lesson.domain;

import java.util.Objects;

public record SceneBlock(
        SceneBlockId id,
        LessonSceneId lessonSceneId,
        long position,
        SceneBlockType type,
        String plainText) {

    private static final int MAX_PLAIN_TEXT_LENGTH = 16_384;

    public SceneBlock {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(lessonSceneId, "lessonSceneId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(plainText, "plainText must not be null");
        if (position < 0) {
            throw new IllegalArgumentException("position must not be negative");
        }
        int plainTextLength = plainText.codePointCount(0, plainText.length());
        if (plainText.isEmpty() || plainTextLength > MAX_PLAIN_TEXT_LENGTH) {
            throw new IllegalArgumentException("plainText length must be between 1 and 16384");
        }
    }
}
