package io.github.sipratama.penatika.classroom.application;

import java.util.Objects;
import java.util.Optional;

import io.github.sipratama.penatika.classroom.application.port.in.AcknowledgeDisplaySynchronizationUseCase;
import io.github.sipratama.penatika.classroom.application.port.out.DisplaySynchronizationAcknowledgementPort;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

public final class AcknowledgeDisplaySynchronizationApplicationService
        implements AcknowledgeDisplaySynchronizationUseCase {

    private final ClassroomDisplayAuthorityApplicationService displayAuthority;
    private final DisplaySynchronizationAcknowledgementPort synchronizationGateway;

    public AcknowledgeDisplaySynchronizationApplicationService(
            ClassroomDisplayAuthorityApplicationService displayAuthority,
            DisplaySynchronizationAcknowledgementPort synchronizationGateway) {
        this.displayAuthority = Objects.requireNonNull(displayAuthority, "displayAuthority must not be null");
        this.synchronizationGateway =
                Objects.requireNonNull(synchronizationGateway, "synchronizationGateway must not be null");
    }

    @Override
    public void acknowledge(
            String classroomSessionId,
            Optional<RawSecurityToken> participantCredential,
            boolean validDisplayIntent,
            long revision) {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantCredential, "participantCredential must not be null");
        var participant = displayAuthority.resolveDisplayParticipant(participantCredential);
        if (!validDisplayIntent) {
            throw new DisplayIntentRequiredException();
        }
        var session = displayAuthority.loadAccessibleSession(classroomSessionId, participant);
        boolean accepted = synchronizationGateway.acknowledge(
                session.id(),
                participant.participantSessionId(),
                new Revision(revision),
                session.revision());
        if (!accepted) {
            throw new DisplaySynchronizationConflictException();
        }
    }
}
