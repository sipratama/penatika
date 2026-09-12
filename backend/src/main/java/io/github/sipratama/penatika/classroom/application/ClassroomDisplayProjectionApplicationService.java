package io.github.sipratama.penatika.classroom.application;

import java.util.Objects;

import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayBlock;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayProjection;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayScene;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveClassroomLessonSceneUseCase;

/**
 * The single Classroom-owned derivation path from one authoritative loaded ClassroomSession
 * to the closed schema 1.0 PLAIN_TEXT Display projection. Reused by the snapshot GET, the
 * initial/reconnect SSE dispatch, and the post-commit projection publisher so the projection
 * shape is derived in exactly one place.
 */
public final class ClassroomDisplayProjectionApplicationService {

    private final ResolveClassroomLessonSceneUseCase lessonScenes;

    public ClassroomDisplayProjectionApplicationService(ResolveClassroomLessonSceneUseCase lessonScenes) {
        this.lessonScenes = Objects.requireNonNull(lessonScenes, "lessonScenes must not be null");
    }

    public ClassroomDisplayProjection deriveProjection(ClassroomSession session) {
        Objects.requireNonNull(session, "session must not be null");
        var source = lessonScenes.resolveScene(session.lessonVersionId(), session.currentScenePosition())
                .orElseThrow(() -> new IllegalStateException("Display projection source is unavailable"));
        if (source.position() != session.currentScenePosition()) {
            throw new IllegalStateException("Display projection scene position does not match Classroom state");
        }
        return new ClassroomDisplayProjection(
                "1.0",
                session.id().value().toString(),
                session.revision().value(),
                new ClassroomDisplayScene(
                        source.sceneId(),
                        source.position(),
                        source.blocks().stream()
                                .map(block -> new ClassroomDisplayBlock(block.type(), block.plainText()))
                                .toList()));
    }
}
