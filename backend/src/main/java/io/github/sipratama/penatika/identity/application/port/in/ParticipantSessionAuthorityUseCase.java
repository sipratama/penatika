package io.github.sipratama.penatika.identity.application.port.in;

import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.identity.application.model.EstablishedParticipantSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;

public interface ParticipantSessionAuthorityUseCase {

    boolean hasEffectiveController(UUID classroomSessionId, UUID classroomOwnerTeacherAccountId);

    boolean hasEffectiveDisplay(UUID classroomSessionId);

    EstablishedParticipantSession establishController(
            UUID classroomSessionId,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId);

    EstablishedParticipantSession establishDisplay(UUID classroomSessionId);

    Optional<ResolvedParticipantSession> resolve(RawSecurityToken credential);
}
