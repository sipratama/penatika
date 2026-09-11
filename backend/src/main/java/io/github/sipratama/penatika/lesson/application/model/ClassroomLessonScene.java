package io.github.sipratama.penatika.lesson.application.model;

import java.util.List;
import java.util.Objects;

public record ClassroomLessonScene(
        String sceneId,
        long position,
        List<ClassroomLessonSceneBlock> blocks) {

    private static final long MAX_WIRE_INTEGER = 9_007_199_254_740_991L;
    private static final int MAX_BLOCKS = 64;

    public ClassroomLessonScene {
        Objects.requireNonNull(sceneId, "sceneId must not be null");
        blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks must not be null"));
        int sceneIdLength = sceneId.codePointCount(0, sceneId.length());
        if (sceneIdLength < 1
                || sceneIdLength > 128
                || sceneId.codePoints().anyMatch(Character::isWhitespace)
                || position < 0
                || position > MAX_WIRE_INTEGER
                || blocks.isEmpty()
                || blocks.size() > MAX_BLOCKS) {
            throw new IllegalArgumentException("invalid classroom lesson scene");
        }
    }
}
