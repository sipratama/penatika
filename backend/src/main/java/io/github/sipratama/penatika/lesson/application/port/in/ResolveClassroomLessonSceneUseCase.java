package io.github.sipratama.penatika.lesson.application.port.in;

import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.lesson.application.model.ClassroomLessonScene;

public interface ResolveClassroomLessonSceneUseCase {

    Optional<ClassroomLessonScene> resolveScene(UUID selectedLessonVersionId, long exactScenePosition);
}
