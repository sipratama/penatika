package io.github.sipratama.penatika.lesson.application;

import java.util.Objects;
import java.util.OptionalLong;
import java.util.UUID;

import io.github.sipratama.penatika.lesson.application.port.in.ResolveNextLessonSceneUseCase;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;
import io.github.sipratama.penatika.lesson.domain.LessonVersionId;

public final class ClassroomLessonNavigationApplicationService
        implements ResolveNextLessonSceneUseCase {

    private final LessonVersionPersistencePort lessonVersions;

    public ClassroomLessonNavigationApplicationService(
            LessonVersionPersistencePort lessonVersions) {
        this.lessonVersions = Objects.requireNonNull(lessonVersions, "lessonVersions must not be null");
    }

    @Override
    public OptionalLong resolveNextScenePosition(
            UUID selectedLessonVersionId,
            long currentScenePosition) {
        Objects.requireNonNull(selectedLessonVersionId, "selectedLessonVersionId must not be null");
        if (currentScenePosition < 0) {
            throw new IllegalArgumentException("currentScenePosition must not be negative");
        }
        return lessonVersions.findById(new LessonVersionId(selectedLessonVersionId))
                .filter(version -> version.isEligibleForClassroomStart())
                .flatMap(version -> version.scenes().stream()
                        .filter(scene -> scene.position() > currentScenePosition)
                        .findFirst())
                .map(scene -> OptionalLong.of(scene.position()))
                .orElseGet(OptionalLong::empty);
    }
}
