package io.github.sipratama.penatika.classroom.application;

import java.util.Objects;
import java.util.Optional;

import io.github.sipratama.penatika.classroom.application.model.EstablishedDisplayStream;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishClassroomDisplayStreamUseCase;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public final class EstablishClassroomDisplayStreamApplicationService
        implements EstablishClassroomDisplayStreamUseCase {

    private final ClassroomDisplayAuthorityApplicationService displayAuthority;
    private final ClassroomDisplayProjectionApplicationService projections;

    public EstablishClassroomDisplayStreamApplicationService(
            ClassroomDisplayAuthorityApplicationService displayAuthority,
            ClassroomDisplayProjectionApplicationService projections) {
        this.displayAuthority = Objects.requireNonNull(displayAuthority, "displayAuthority must not be null");
        this.projections = Objects.requireNonNull(projections, "projections must not be null");
    }

    @Override
    public EstablishedDisplayStream establish(
            String classroomSessionId,
            Optional<RawSecurityToken> participantCredential) {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantCredential, "participantCredential must not be null");
        var participant = displayAuthority.resolveDisplayParticipant(participantCredential);
        var session = displayAuthority.loadAccessibleSession(classroomSessionId, participant);
        var projection = projections.deriveProjection(session);
        return new EstablishedDisplayStream(session.id(), participant.participantSessionId(), projection);
    }
}
