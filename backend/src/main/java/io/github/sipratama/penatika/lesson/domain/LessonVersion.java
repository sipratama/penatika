package io.github.sipratama.penatika.lesson.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record LessonVersion(
        LessonVersionId id,
        LessonId lessonId,
        LessonVersionReadiness readiness,
        Instant createdAt,
        List<LessonScene> scenes) {

    private static final int MAX_BLOCKS_PER_SCENE = 64;
    private static final int MAX_PLAIN_TEXT_LENGTH = 16_384;

    public LessonVersion {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(lessonId, "lessonId must not be null");
        Objects.requireNonNull(readiness, "readiness must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        scenes = List.copyOf(Objects.requireNonNull(scenes, "scenes must not be null"));
    }

    public boolean isClassroomReady() {
        return readiness == LessonVersionReadiness.CLASSROOM_READY;
    }

    public boolean isEligibleForClassroomStart() {
        if (!isClassroomReady() || scenes.isEmpty()) {
            return false;
        }
        for (int sceneIndex = 0; sceneIndex < scenes.size(); sceneIndex++) {
            LessonScene scene = scenes.get(sceneIndex);
            if (!scene.lessonVersionId().equals(id)
                    || scene.position() != sceneIndex
                    || scene.blocks().isEmpty()
                    || scene.blocks().size() > MAX_BLOCKS_PER_SCENE) {
                return false;
            }
            for (int blockIndex = 0; blockIndex < scene.blocks().size(); blockIndex++) {
                SceneBlock block = scene.blocks().get(blockIndex);
                String plainText = block.plainText();
                int plainTextLength = plainText.codePointCount(0, plainText.length());
                if (!block.lessonSceneId().equals(scene.id())
                        || block.position() != blockIndex
                        || block.type() != SceneBlockType.PLAIN_TEXT
                        || plainText.isEmpty()
                        || plainTextLength > MAX_PLAIN_TEXT_LENGTH) {
                    return false;
                }
            }
        }
        return true;
    }
}
