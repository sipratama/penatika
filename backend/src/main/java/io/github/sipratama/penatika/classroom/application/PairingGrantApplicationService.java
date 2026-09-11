package io.github.sipratama.penatika.classroom.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.IssuedPairingGrant;
import io.github.sipratama.penatika.classroom.application.model.RawPairingToken;
import io.github.sipratama.penatika.classroom.application.port.in.IssuePairingGrantUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.RevokePairingGrantUseCase;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingGrantPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingTokenGeneratorPort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingTokenVerifierPort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.PairingGrant;
import io.github.sipratama.penatika.classroom.domain.PairingGrantId;
import io.github.sipratama.penatika.classroom.domain.PairingRole;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;

public final class PairingGrantApplicationService
        implements IssuePairingGrantUseCase, RevokePairingGrantUseCase {

    private final ClassroomSessionPersistencePort classroomSessions;
    private final PairingGrantPersistencePort pairingGrants;
    private final ParticipantSessionAuthorityUseCase participantSessions;
    private final PairingTokenGeneratorPort tokenGenerator;
    private final PairingTokenVerifierPort tokenVerifier;
    private final Clock clock;

    public PairingGrantApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            PairingGrantPersistencePort pairingGrants,
            ParticipantSessionAuthorityUseCase participantSessions,
            PairingTokenGeneratorPort tokenGenerator,
            PairingTokenVerifierPort tokenVerifier,
            Clock clock) {
        this.classroomSessions = Objects.requireNonNull(classroomSessions, "classroomSessions must not be null");
        this.pairingGrants = Objects.requireNonNull(pairingGrants, "pairingGrants must not be null");
        this.participantSessions = Objects.requireNonNull(participantSessions, "participantSessions must not be null");
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator, "tokenGenerator must not be null");
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier, "tokenVerifier must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public IssuedPairingGrant issue(
            String classroomSessionId,
            String participantRole,
            UUID teacherAccountId) {
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        ClassroomSession session = requireOwnedSession(classroomSessionId, teacherAccountId);
        if (!session.isPairable()) {
            throw new ClassroomSessionNotPairableException();
        }

        PairingRole role = PairingRole.valueOf(participantRole);
        boolean occupied = switch (role) {
            case TEACHER_CONTROLLER -> participantSessions.hasEffectiveController(
                    session.id().value(), teacherAccountId);
            case CLASSROOM_DISPLAY -> participantSessions.hasEffectiveDisplay(session.id().value());
        };
        if (occupied) {
            throw new ParticipantRoleAlreadyActiveException();
        }

        Instant issuedAt = clock.instant();
        RawPairingToken rawToken = tokenGenerator.generate();
        PairingGrant grant = PairingGrant.issue(
                new PairingGrantId(UUID.randomUUID()),
                session.id(),
                role,
                tokenVerifier.verifierFor(rawToken),
                issuedAt);
        pairingGrants.create(grant);
        return new IssuedPairingGrant(
                grant.id().value().toString(),
                rawToken,
                role.name(),
                grant.expiresAt());
    }

    @Override
    public void revoke(String classroomSessionId, String pairingGrantId, UUID teacherAccountId) {
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        ClassroomSession session = requireOwnedSession(classroomSessionId, teacherAccountId);
        parseUuid(pairingGrantId).ifPresent(id -> pairingGrants.revoke(
                session.id(), new PairingGrantId(id), clock.instant()));
    }

    private ClassroomSession requireOwnedSession(String externalId, UUID teacherAccountId) {
        UUID internalId = parseUuid(externalId).orElseThrow(ClassroomSessionNotFoundException::new);
        return classroomSessions.findById(new ClassroomSessionId(internalId))
                .filter(session -> session.teacherAccountId().equals(teacherAccountId))
                .orElseThrow(ClassroomSessionNotFoundException::new);
    }

    private static java.util.Optional<UUID> parseUuid(String value) {
        try {
            UUID parsed = UUID.fromString(value);
            if (!parsed.toString().equalsIgnoreCase(value)) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(parsed);
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }
}
