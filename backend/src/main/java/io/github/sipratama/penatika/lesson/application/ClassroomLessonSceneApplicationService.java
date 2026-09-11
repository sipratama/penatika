package io.github.sipratama.penatika.lesson.application;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.lesson.application.model.ClassroomLessonScene;
import io.github.sipratama.penatika.lesson.application.model.ClassroomLessonSceneBlock;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveClassroomLessonSceneUseCase;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;
import io.github.sipratama.penatika.lesson.domain.LessonVersionId;

public final class ClassroomLessonSceneApplicationService
        implements ResolveClassroomLessonSceneUseCase {

    private final LessonVersionPersistencePort lessonVersions;

    public ClassroomLessonSceneApplicationService(LessonVersionPersistencePort lessonVersions) {
        this.lessonVersions = Objects.requireNonNull(lessonVersions, "lessonVersions must not be null");
    }

    @Override
    public Optional<ClassroomLessonScene> resolveScene(
            UUID selectedLessonVersionId,
            long exactScenePosition) {
        Objects.requireNonNull(selectedLessonVersionId, "selectedLessonVersionId must not be null");
        if (exactScenePosition < 0) {
            throw new IllegalArgumentException("exactScenePosition must not be negative");
        }
        return lessonVersions.findById(new LessonVersionId(selectedLessonVersionId))
                .filter(version -> version.isEligibleForClassroomStart())
                .flatMap(version -> version.scenes().stream()
                        .filter(scene -> scene.position() == exactScenePosition)
                        .findFirst())
                .map(scene -> new ClassroomLessonScene(
                        scene.id().value().toString(),
                        scene.position(),
                        scene.blocks().stream()
                                .map(block -> new ClassroomLessonSceneBlock(
                                        block.type().name(), block.plainText()))
                                .toList()));
    }
}
