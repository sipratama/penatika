package io.github.sipratama.penatika.classroom.application.model;

import java.util.List;
import java.util.Objects;

public record ClassroomDisplayScene(
        String sceneId,
        long position,
        List<ClassroomDisplayBlock> blocks) {

    private static final long MAX_WIRE_INTEGER = 9_007_199_254_740_991L;

    public ClassroomDisplayScene {
        Objects.requireNonNull(sceneId, "sceneId must not be null");
        blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks must not be null"));
        int idLength = sceneId.codePointCount(0, sceneId.length());
        if (idLength < 1
                || idLength > 128
                || sceneId.codePoints().anyMatch(ClassroomDisplayScene::isWhitespace)
                || position < 0
                || position > MAX_WIRE_INTEGER
                || blocks.isEmpty()
                || blocks.size() > 64) {
            throw new IllegalArgumentException("invalid Display projection scene");
        }
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }
}
