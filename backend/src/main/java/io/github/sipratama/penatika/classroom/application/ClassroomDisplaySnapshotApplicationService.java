package io.github.sipratama.penatika.classroom.application;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayBlock;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayProjection;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayScene;
import io.github.sipratama.penatika.classroom.application.port.in.GetClassroomDisplaySnapshotUseCase;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveClassroomLessonSceneUseCase;

public final class ClassroomDisplaySnapshotApplicationService
        implements GetClassroomDisplaySnapshotUseCase {

    private static final String DISPLAY_ROLE = "CLASSROOM_DISPLAY";

    private final ParticipantSessionAuthorityUseCase participantSessions;
    private final ClassroomSessionPersistencePort classroomSessions;
    private final ResolveClassroomLessonSceneUseCase lessonScenes;

    public ClassroomDisplaySnapshotApplicationService(
            ParticipantSessionAuthorityUseCase participantSessions,
            ClassroomSessionPersistencePort classroomSessions,
            ResolveClassroomLessonSceneUseCase lessonScenes) {
        this.participantSessions = Objects.requireNonNull(participantSessions, "participantSessions must not be null");
        this.classroomSessions = Objects.requireNonNull(classroomSessions, "classroomSessions must not be null");
        this.lessonScenes = Objects.requireNonNull(lessonScenes, "lessonScenes must not be null");
    }

    @Override
    public ClassroomDisplayProjection getSnapshot(
            String classroomSessionId,
            Optional<RawSecurityToken> participantCredential) {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantCredential, "participantCredential must not be null");
        ResolvedParticipantSession participant = participantCredential
                .flatMap(participantSessions::resolve)
                .orElseThrow(DisplaySessionRequiredException::new);
        if (!DISPLAY_ROLE.equals(participant.participantRole())) {
            throw new DisplayAuthorityRequiredException();
        }

        UUID internalId = parseCanonicalUuid(classroomSessionId);
        if (!participant.classroomSessionId().equals(internalId)) {
            throw new ClassroomSessionNotFoundException();
        }
        ClassroomSession session = classroomSessions.findById(new ClassroomSessionId(internalId))
                .filter(ClassroomSession::permitsDisplayAccess)
                .orElseThrow(ClassroomSessionNotFoundException::new);
        var source = lessonScenes.resolveScene(
                        session.lessonVersionId(), session.currentScenePosition())
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

    private static UUID parseCanonicalUuid(String value) {
        try {
            UUID parsed = UUID.fromString(value);
            if (!parsed.toString().equalsIgnoreCase(value)) {
                throw new ClassroomSessionNotFoundException();
            }
            return parsed;
        } catch (IllegalArgumentException exception) {
            throw new ClassroomSessionNotFoundException();
        }
    }
}
