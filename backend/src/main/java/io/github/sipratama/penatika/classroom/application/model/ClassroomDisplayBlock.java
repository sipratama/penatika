package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;

public record ClassroomDisplayBlock(String type, String text) {

    private static final int MAX_TEXT_CODE_POINTS = 16_384;

    public ClassroomDisplayBlock {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(text, "text must not be null");
        int length = text.codePointCount(0, text.length());
        if (!"PLAIN_TEXT".equals(type) || length < 1 || length > MAX_TEXT_CODE_POINTS) {
            throw new IllegalArgumentException("invalid Display projection block");
        }
    }
}
