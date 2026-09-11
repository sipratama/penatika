package io.github.sipratama.penatika.classroom.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.EstablishedParticipant;
import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingGrantPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingTokenVerifierPort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.PairingGrant;
import io.github.sipratama.penatika.classroom.domain.PairingRole;
import io.github.sipratama.penatika.identity.application.ParticipantRoleOccupiedException;
import io.github.sipratama.penatika.identity.application.ParticipantTeacherAuthorityInvalidException;
import io.github.sipratama.penatika.identity.application.model.EstablishedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;

public final class PairingGrantRedemptionApplicationService {

    private final PairingGrantPersistencePort pairingGrants;
    private final ClassroomSessionPersistencePort classroomSessions;
    private final ParticipantSessionAuthorityUseCase participantSessions;
    private final PairingTokenVerifierPort tokenVerifier;
    private final Clock clock;

    public PairingGrantRedemptionApplicationService(
            PairingGrantPersistencePort pairingGrants,
            ClassroomSessionPersistencePort classroomSessions,
            ParticipantSessionAuthorityUseCase participantSessions,
            PairingTokenVerifierPort tokenVerifier,
            Clock clock) {
        this.pairingGrants = Objects.requireNonNull(pairingGrants, "pairingGrants must not be null");
        this.classroomSessions = Objects.requireNonNull(classroomSessions, "classroomSessions must not be null");
        this.participantSessions = Objects.requireNonNull(participantSessions, "participantSessions must not be null");
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier, "tokenVerifier must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public EstablishedParticipant establishController(
            PresentedPairingToken presentedToken,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId) {
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(teacherBrowserSessionId, "teacherBrowserSessionId must not be null");
        PairingGrant grant = claim(presentedToken, PairingRole.TEACHER_CONTROLLER);
        ClassroomSession classroomSession = requirePairable(grant);
        if (!classroomSession.teacherAccountId().equals(teacherAccountId)) {
            throw new PairingGrantRejectedException();
        }
        try {
            EstablishedParticipantSession participant = participantSessions.establishController(
                    classroomSession.id().value(), teacherAccountId, teacherBrowserSessionId);
            return established(participant, PairingRole.TEACHER_CONTROLLER);
        } catch (ParticipantRoleOccupiedException exception) {
            throw new ParticipantRoleAlreadyActiveException();
        } catch (ParticipantTeacherAuthorityInvalidException exception) {
            throw new PairingGrantRejectedException();
        }
    }

    public EstablishedParticipant establishDisplay(PresentedPairingToken presentedToken) {
        PairingGrant grant = claim(presentedToken, PairingRole.CLASSROOM_DISPLAY);
        ClassroomSession classroomSession = requirePairable(grant);
        try {
            EstablishedParticipantSession participant = participantSessions.establishDisplay(
                    classroomSession.id().value());
            return established(participant, PairingRole.CLASSROOM_DISPLAY);
        } catch (ParticipantRoleOccupiedException exception) {
            throw new ParticipantRoleAlreadyActiveException();
        }
    }

    private PairingGrant claim(PresentedPairingToken presentedToken, PairingRole requiredRole) {
        Objects.requireNonNull(presentedToken, "presentedToken must not be null");
        Instant now = clock.instant();
        PairingGrant grant = pairingGrants
                .consumeByCredentialVerifier(tokenVerifier.verifierFor(presentedToken), now)
                .orElseThrow(PairingGrantRejectedException::new);
        if (grant.participantRole() != requiredRole) {
            throw new PairingGrantRejectedException();
        }
        return grant;
    }

    private ClassroomSession requirePairable(PairingGrant grant) {
        return classroomSessions.findById(grant.classroomSessionId())
                .filter(ClassroomSession::isPairable)
                .orElseThrow(PairingGrantRejectedException::new);
    }

    private static EstablishedParticipant established(
            EstablishedParticipantSession participant,
            PairingRole role) {
        return new EstablishedParticipant(
                participant.classroomSessionId().toString(),
                role.name(),
                participant.credential(),
                participant.createdAt(),
                participant.expiresAt());
    }
}
