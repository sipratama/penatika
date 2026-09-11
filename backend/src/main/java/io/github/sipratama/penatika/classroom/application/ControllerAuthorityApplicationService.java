package io.github.sipratama.penatika.classroom.application;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.AuthorizedController;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;

public final class ControllerAuthorityApplicationService {

    private static final String CONTROLLER_ROLE = "TEACHER_CONTROLLER";

    private final ParticipantSessionAuthorityUseCase participantSessions;

    public ControllerAuthorityApplicationService(
            ParticipantSessionAuthorityUseCase participantSessions) {
        this.participantSessions = Objects.requireNonNull(participantSessions);
    }

    public AuthorizedController requireAuthority(
            ClassroomSession classroomSession,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId,
            Optional<RawSecurityToken> participantCredential) {
        if (!classroomSession.teacherAccountId().equals(teacherAccountId)) {
            throw new ClassroomSessionNotFoundException();
        }
        ResolvedParticipantSession participant = participantCredential
                .flatMap(participantSessions::resolve)
                .filter(candidate -> CONTROLLER_ROLE.equals(candidate.participantRole()))
                .filter(candidate -> candidate.classroomSessionId().equals(classroomSession.id().value()))
                .filter(candidate -> candidate.teacherAccountIdOptional().filter(teacherAccountId::equals).isPresent())
                .filter(candidate -> candidate.teacherBrowserSessionIdOptional()
                        .filter(teacherBrowserSessionId::equals)
                        .isPresent())
                .orElseThrow(ControllerAuthorityRequiredException::new);
        return new AuthorizedController(
                teacherAccountId, teacherBrowserSessionId, participant.participantSessionId());
    }
}
