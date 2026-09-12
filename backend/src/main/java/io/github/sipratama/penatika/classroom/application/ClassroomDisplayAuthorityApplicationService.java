package io.github.sipratama.penatika.classroom.application;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;

/**
 * Centralizes the Display participant/role/binding/accessibility checks shared by the
 * snapshot, SSE, and synchronization operations so each keeps identical non-disclosing
 * 401/403/404 semantics without duplicating the resolution order.
 */
public final class ClassroomDisplayAuthorityApplicationService {

    private static final String DISPLAY_ROLE = "CLASSROOM_DISPLAY";

    private final ParticipantSessionAuthorityUseCase participantSessions;
    private final ClassroomSessionPersistencePort classroomSessions;

    public ClassroomDisplayAuthorityApplicationService(
            ParticipantSessionAuthorityUseCase participantSessions,
            ClassroomSessionPersistencePort classroomSessions) {
        this.participantSessions = Objects.requireNonNull(participantSessions, "participantSessions must not be null");
        this.classroomSessions = Objects.requireNonNull(classroomSessions, "classroomSessions must not be null");
    }

    public ResolvedParticipantSession resolveDisplayParticipant(Optional<RawSecurityToken> participantCredential) {
        Objects.requireNonNull(participantCredential, "participantCredential must not be null");
        ResolvedParticipantSession participant = participantCredential
                .flatMap(participantSessions::resolve)
                .orElseThrow(DisplaySessionRequiredException::new);
        if (!DISPLAY_ROLE.equals(participant.participantRole())) {
            throw new DisplayAuthorityRequiredException();
        }
        return participant;
    }

    public ClassroomSession loadAccessibleSession(
            String classroomSessionId, ResolvedParticipantSession participant) {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participant, "participant must not be null");
        UUID internalId = parseCanonicalUuid(classroomSessionId);
        if (!participant.classroomSessionId().equals(internalId)) {
            throw new ClassroomSessionNotFoundException();
        }
        return classroomSessions.findById(new ClassroomSessionId(internalId))
                .filter(ClassroomSession::permitsDisplayAccess)
                .orElseThrow(ClassroomSessionNotFoundException::new);
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
