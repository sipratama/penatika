package io.github.sipratama.penatika.identity.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherMutation;
import io.github.sipratama.penatika.identity.application.model.EstablishedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.ExternalTeacherIdentity;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.TeacherSessionBootstrap;
import io.github.sipratama.penatika.identity.application.port.in.AuthenticateTeacherBrowserSessionUseCase;
import io.github.sipratama.penatika.identity.application.port.in.AuthorizeTeacherMutationUseCase;
import io.github.sipratama.penatika.identity.application.port.in.BootstrapTeacherBrowserSessionUseCase;
import io.github.sipratama.penatika.identity.application.port.in.EstablishTeacherBrowserSessionUseCase;
import io.github.sipratama.penatika.identity.application.port.in.RevokeTeacherBrowserSessionUseCase;
import io.github.sipratama.penatika.identity.application.port.in.VerifyTeacherCsrfUseCase;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenGeneratorPort;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenVerifierPort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLink;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public final class TeacherBrowserSessionApplicationService implements
        EstablishTeacherBrowserSessionUseCase,
        AuthenticateTeacherBrowserSessionUseCase,
        AuthorizeTeacherMutationUseCase,
        BootstrapTeacherBrowserSessionUseCase,
        VerifyTeacherCsrfUseCase,
        RevokeTeacherBrowserSessionUseCase {

    public static final Duration IDLE_TIMEOUT = Duration.ofMinutes(30);
    public static final Duration ABSOLUTE_TIMEOUT = Duration.ofHours(8);

    private final TeacherIdentityPersistencePort teacherIdentities;
    private final TeacherBrowserSessionPersistencePort browserSessions;
    private final SecurityTokenGeneratorPort tokenGenerator;
    private final SecurityTokenVerifierPort tokenVerifier;
    private final Clock clock;

    public TeacherBrowserSessionApplicationService(
            TeacherIdentityPersistencePort teacherIdentities,
            TeacherBrowserSessionPersistencePort browserSessions,
            SecurityTokenGeneratorPort tokenGenerator,
            SecurityTokenVerifierPort tokenVerifier,
            Clock clock) {
        this.teacherIdentities = Objects.requireNonNull(teacherIdentities);
        this.browserSessions = Objects.requireNonNull(browserSessions);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public EstablishedTeacherSession establish(
            ExternalTeacherIdentity externalIdentity,
            Optional<RawSecurityToken> currentBrowserCredential) {
        ExternalIdentityLink identityLink = teacherIdentities
                .findExternalIdentity(externalIdentity.issuer(), externalIdentity.subject())
                .orElseThrow(TeacherAuthenticationRejectedException::new);
        TeacherAccount teacherAccount = teacherIdentities
                .findTeacherAccount(identityLink.teacherAccountId())
                .filter(TeacherAccount::isActive)
                .orElseThrow(TeacherAuthenticationRejectedException::new);

        Instant now = clock.instant();
        RawSecurityToken sessionCredential = tokenGenerator.generate();
        RawSecurityToken csrfToken = tokenGenerator.generate();
        TeacherBrowserSession session = new TeacherBrowserSession(
                new TeacherBrowserSessionId(UUID.randomUUID()),
                teacherAccount.id(),
                tokenVerifier.verifierFor(sessionCredential),
                tokenVerifier.verifierFor(csrfToken),
                now,
                now,
                now.plus(IDLE_TIMEOUT),
                now.plus(ABSOLUTE_TIMEOUT),
                null);
        Optional<String> replacedVerifier = currentBrowserCredential.map(tokenVerifier::verifierFor);
        browserSessions.createReplacing(session, replacedVerifier, now);
        return new EstablishedTeacherSession(sessionCredential, csrfToken, session.absoluteExpiresAt());
    }

    @Override
    public Optional<AuthenticatedTeacherSession> authenticate(RawSecurityToken credential) {
        return authenticatePersistedSession(
                browserSessions.findByCredentialVerifier(tokenVerifier.verifierFor(credential)));
    }

    @Override
    public TeacherSessionBootstrap bootstrap(
            AuthenticatedTeacherSession authenticatedSession,
            Optional<RawSecurityToken> csrfRecoveryToken) {
        TeacherBrowserSession currentSession = requireCurrentUsableSession(
                authenticatedSession.session().id()).session();
        Instant now = clock.instant();
        Instant idleExpiresAt = currentSession.idleExpiryAfterActivity(now, IDLE_TIMEOUT);

        if (csrfRecoveryToken
                .filter(token -> tokenVerifier.matches(token, currentSession.csrfVerifier()))
                .isPresent()) {
            if (!browserSessions.refreshActivity(currentSession.id(), now, idleExpiresAt)) {
                throw new TeacherSessionRequiredException();
            }
            return new TeacherSessionBootstrap(
                    csrfRecoveryToken.orElseThrow(), currentSession.absoluteExpiresAt(), false);
        }

        RawSecurityToken replacement = tokenGenerator.generate();
        String replacementVerifier = tokenVerifier.verifierFor(replacement);
        if (!browserSessions.replaceCsrfVerifierAndRefreshActivity(
                currentSession.id(),
                currentSession.csrfVerifier(),
                replacementVerifier,
                now,
                idleExpiresAt)) {
            throw new TeacherSessionRequiredException();
        }
        return new TeacherSessionBootstrap(replacement, currentSession.absoluteExpiresAt(), true);
    }

    @Override
    public boolean verify(
            AuthenticatedTeacherSession authenticatedSession,
            Optional<RawSecurityToken> presentedToken) {
        return currentUsableSession(authenticatedSession.session().id())
                .map(AuthenticatedTeacherSession::session)
                .filter(session -> presentedToken
                        .filter(token -> tokenVerifier.matches(token, session.csrfVerifier()))
                        .isPresent())
                .isPresent();
    }

    @Override
    public AuthorizedTeacherMutation authorizeMutation(
            AuthenticatedTeacherSession authenticatedSession,
            Optional<RawSecurityToken> presentedToken) {
        AuthenticatedTeacherSession currentSession = requireCurrentUsableSession(
                authenticatedSession.session().id());
        if (presentedToken
                .filter(token -> tokenVerifier.matches(token, currentSession.session().csrfVerifier()))
                .isEmpty()) {
            throw new TeacherCsrfRejectedException();
        }

        Instant now = clock.instant();
        Instant idleExpiresAt = currentSession.session().idleExpiryAfterActivity(now, IDLE_TIMEOUT);
        if (!browserSessions.refreshActivity(currentSession.session().id(), now, idleExpiresAt)) {
            throw new TeacherSessionRequiredException();
        }
        return new AuthorizedTeacherMutation(
                currentSession.teacherAccount().id().value(),
                currentSession.session().id().value());
    }

    @Override
    public boolean revoke(TeacherBrowserSessionId sessionId) {
        return browserSessions.revoke(sessionId, clock.instant());
    }

    private Optional<AuthenticatedTeacherSession> authenticatePersistedSession(
            Optional<TeacherBrowserSession> possibleSession) {
        Instant now = clock.instant();
        return possibleSession
                .filter(session -> session.isUsableAt(now))
                .flatMap(session -> teacherIdentities.findTeacherAccount(session.teacherAccountId())
                        .filter(TeacherAccount::isActive)
                        .map(account -> new AuthenticatedTeacherSession(account, session)));
    }

    private AuthenticatedTeacherSession requireCurrentUsableSession(TeacherBrowserSessionId sessionId) {
        return currentUsableSession(sessionId).orElseThrow(TeacherSessionRequiredException::new);
    }

    private Optional<AuthenticatedTeacherSession> currentUsableSession(TeacherBrowserSessionId sessionId) {
        return authenticatePersistedSession(browserSessions.findById(sessionId));
    }
}
