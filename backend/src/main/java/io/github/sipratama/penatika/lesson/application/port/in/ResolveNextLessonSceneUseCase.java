package io.github.sipratama.penatika.lesson.application.port.in;

import java.util.OptionalLong;
import java.util.UUID;

public interface ResolveNextLessonSceneUseCase {

    OptionalLong resolveNextScenePosition(UUID selectedLessonVersionId, long currentScenePosition);
}
