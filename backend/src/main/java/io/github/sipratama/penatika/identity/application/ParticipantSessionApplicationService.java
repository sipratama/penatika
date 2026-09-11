package io.github.sipratama.penatika.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.identity.application.model.EstablishedParticipantSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;
import io.github.sipratama.penatika.identity.application.port.out.ParticipantSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenGeneratorPort;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenVerifierPort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;
import io.github.sipratama.penatika.identity.domain.ClassroomSessionReference;
import io.github.sipratama.penatika.identity.domain.ParticipantRole;
import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.ParticipantSessionId;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public final class ParticipantSessionApplicationService implements ParticipantSessionAuthorityUseCase {

    private final ParticipantSessionPersistencePort participantSessions;
    private final TeacherIdentityPersistencePort teacherIdentities;
    private final TeacherBrowserSessionPersistencePort teacherBrowserSessions;
    private final SecurityTokenGeneratorPort tokenGenerator;
    private final SecurityTokenVerifierPort tokenVerifier;
    private final Clock clock;

    public ParticipantSessionApplicationService(
            ParticipantSessionPersistencePort participantSessions,
            TeacherIdentityPersistencePort teacherIdentities,
            TeacherBrowserSessionPersistencePort teacherBrowserSessions,
            SecurityTokenGeneratorPort tokenGenerator,
            SecurityTokenVerifierPort tokenVerifier,
            Clock clock) {
        this.participantSessions = Objects.requireNonNull(participantSessions, "participantSessions must not be null");
        this.teacherIdentities = Objects.requireNonNull(teacherIdentities, "teacherIdentities must not be null");
        this.teacherBrowserSessions = Objects.requireNonNull(
                teacherBrowserSessions, "teacherBrowserSessions must not be null");
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator, "tokenGenerator must not be null");
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier, "tokenVerifier must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public boolean hasEffectiveController(
            UUID classroomSessionId,
            UUID classroomOwnerTeacherAccountId) {
        return effectiveOccupant(
                        new ClassroomSessionReference(classroomSessionId),
                        ParticipantRole.TEACHER_CONTROLLER,
                        new TeacherAccountId(classroomOwnerTeacherAccountId),
                        clock.instant())
                .isPresent();
    }

    @Override
    public boolean hasEffectiveDisplay(UUID classroomSessionId) {
        return effectiveOccupant(
                        new ClassroomSessionReference(classroomSessionId),
                        ParticipantRole.CLASSROOM_DISPLAY,
                        null,
                        clock.instant())
                .isPresent();
    }

    @Override
    public EstablishedParticipantSession establishController(
            UUID classroomSessionId,
            UUID teacherAccountId,
            UUID teacherBrowserSessionId) {
        ClassroomSessionReference sessionId = new ClassroomSessionReference(classroomSessionId);
        TeacherAccountId accountId = new TeacherAccountId(teacherAccountId);
        TeacherBrowserSessionId browserSessionId = new TeacherBrowserSessionId(teacherBrowserSessionId);
        Instant now = clock.instant();
        if (effectiveOccupant(sessionId, ParticipantRole.TEACHER_CONTROLLER, accountId, now).isPresent()) {
            throw new ParticipantRoleOccupiedException();
        }
        requireUsableTeacherContext(accountId, browserSessionId, now);
        RawSecurityToken credential = tokenGenerator.generate();
        return create(ParticipantSession.controller(
                new ParticipantSessionId(UUID.randomUUID()),
                sessionId,
                tokenVerifier.verifierFor(credential),
                accountId,
                browserSessionId,
                now), credential);
    }

    @Override
    public EstablishedParticipantSession establishDisplay(UUID classroomSessionId) {
        ClassroomSessionReference sessionId = new ClassroomSessionReference(classroomSessionId);
        Instant now = clock.instant();
        if (effectiveOccupant(sessionId, ParticipantRole.CLASSROOM_DISPLAY, null, now).isPresent()) {
            throw new ParticipantRoleOccupiedException();
        }
        RawSecurityToken credential = tokenGenerator.generate();
        return create(ParticipantSession.display(
                new ParticipantSessionId(UUID.randomUUID()),
                sessionId,
                tokenVerifier.verifierFor(credential),
                now), credential);
    }

    @Override
    public Optional<ResolvedParticipantSession> resolve(RawSecurityToken credential) {
        Objects.requireNonNull(credential, "credential must not be null");
        Instant now = clock.instant();
        return participantSessions.findByCredentialVerifier(tokenVerifier.verifierFor(credential))
                .filter(session -> authorityIsUsable(session, session.teacherAccountId(), now))
                .map(ParticipantSessionApplicationService::resolved);
    }

    private EstablishedParticipantSession create(
            ParticipantSession participantSession,
            RawSecurityToken credential) {
        if (!participantSessions.tryCreateActive(participantSession)) {
            throw new ParticipantRoleOccupiedException();
        }
        return new EstablishedParticipantSession(
                participantSession.classroomSessionId().value(),
                credential,
                participantSession.createdAt(),
                participantSession.expiresAt());
    }

    private Optional<ParticipantSession> effectiveOccupant(
            ClassroomSessionReference classroomSessionId,
            ParticipantRole role,
            TeacherAccountId classroomOwner,
            Instant now) {
        participantSessions.revokeInvalidLifetimeOccupants(classroomSessionId, role, now);
        Optional<ParticipantSession> occupant = participantSessions.findActiveByRole(classroomSessionId, role);
        if (occupant.isEmpty()) {
            return Optional.empty();
        }
        ParticipantSession session = occupant.orElseThrow();
        if (authorityIsUsable(session, classroomOwner, now)) {
            return occupant;
        }
        participantSessions.revoke(session.id(), now);
        return Optional.empty();
    }

    private boolean authorityIsUsable(
            ParticipantSession session,
            TeacherAccountId classroomOwner,
            Instant now) {
        if (!session.isUsableAt(now)) {
            return false;
        }
        if (session.participantRole() == ParticipantRole.CLASSROOM_DISPLAY) {
            return true;
        }
        if (classroomOwner == null || !classroomOwner.equals(session.teacherAccountId())) {
            return false;
        }
        return teacherContextIsUsable(
                session.teacherAccountId(), session.teacherBrowserSessionId(), now);
    }

    private void requireUsableTeacherContext(
            TeacherAccountId teacherAccountId,
            TeacherBrowserSessionId browserSessionId,
            Instant now) {
        if (!teacherContextIsUsable(teacherAccountId, browserSessionId, now)) {
            throw new TeacherSessionRequiredException();
        }
    }

    private boolean teacherContextIsUsable(
            TeacherAccountId teacherAccountId,
            TeacherBrowserSessionId browserSessionId,
            Instant now) {
        Optional<TeacherAccount> teacher = teacherIdentities.findTeacherAccount(teacherAccountId);
        Optional<TeacherBrowserSession> browserSession = teacherBrowserSessions.findById(browserSessionId);
        return teacher.filter(TeacherAccount::isActive).isPresent()
                && browserSession
                        .filter(session -> session.teacherAccountId().equals(teacherAccountId))
                        .filter(session -> session.isUsableAt(now))
                        .isPresent();
    }

    private static ResolvedParticipantSession resolved(ParticipantSession session) {
        return new ResolvedParticipantSession(
                session.id().value(),
                session.classroomSessionId().value(),
                session.participantRole().name(),
                value(session.teacherAccountId()),
                value(session.teacherBrowserSessionId()),
                session.createdAt(),
                session.expiresAt());
    }

    private static UUID value(TeacherAccountId value) {
        return value == null ? null : value.value();
    }

    private static UUID value(TeacherBrowserSessionId value) {
        return value == null ? null : value.value();
    }
}
