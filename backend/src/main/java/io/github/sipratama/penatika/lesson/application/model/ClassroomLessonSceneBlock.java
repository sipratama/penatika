package io.github.sipratama.penatika.lesson.application.model;

import java.util.Objects;

public record ClassroomLessonSceneBlock(String type, String plainText) {

    private static final int MAX_PLAIN_TEXT_CODE_POINTS = 16_384;

    public ClassroomLessonSceneBlock {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(plainText, "plainText must not be null");
        int length = plainText.codePointCount(0, plainText.length());
        if (!"PLAIN_TEXT".equals(type) || length < 1 || length > MAX_PLAIN_TEXT_CODE_POINTS) {
            throw new IllegalArgumentException("invalid classroom lesson scene block");
        }
    }
}
