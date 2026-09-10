package io.github.sipratama.penatika.lesson.domain;

import java.util.List;
import java.util.Objects;

public record LessonScene(
        LessonSceneId id,
        LessonVersionId lessonVersionId,
        long position,
        List<SceneBlock> blocks) {

    public LessonScene {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(lessonVersionId, "lessonVersionId must not be null");
        if (position < 0) {
            throw new IllegalArgumentException("position must not be negative");
        }
        blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks must not be null"));
    }
}
