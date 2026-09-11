package io.github.sipratama.penatika.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.sipratama.penatika.identity.adapter.out.security.Sha256SecurityTokenVerifier;
import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.EstablishedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.ExternalTeacherIdentity;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.TeacherSessionBootstrap;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenGeneratorPort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLink;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLinkId;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountStatus;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

@ExtendWith(MockitoExtension.class)
class TeacherBrowserSessionApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-10T10:00:00Z");
    private static final String ISSUER = "https://identity.example.test";
    private static final String SUBJECT = "teacher-subject";
    private static final TeacherAccountId TEACHER_ID =
            new TeacherAccountId(UUID.fromString("10000000-0000-0000-0000-000000000101"));
    private static final TeacherBrowserSessionId SESSION_ID =
            new TeacherBrowserSessionId(UUID.fromString("10000000-0000-0000-0000-000000000102"));
    private static final RawSecurityToken SESSION_TOKEN = token('S');
    private static final RawSecurityToken CSRF_TOKEN = token('C');
    private static final RawSecurityToken REPLACEMENT_SESSION_TOKEN = token('N');
    private static final RawSecurityToken REPLACEMENT_CSRF_TOKEN = token('R');

    @Mock private TeacherIdentityPersistencePort teacherIdentities;
    @Mock private TeacherBrowserSessionPersistencePort browserSessions;

    private final Sha256SecurityTokenVerifier verifier = new Sha256SecurityTokenVerifier();
    private QueueTokenGenerator tokenGenerator;
    private TeacherBrowserSessionApplicationService service;

    @BeforeEach
    void setUp() {
        tokenGenerator = new QueueTokenGenerator();
        service = new TeacherBrowserSessionApplicationService(
                teacherIdentities,
                browserSessions,
                tokenGenerator,
                verifier,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void establishesOnlyTheExistingActiveIssuerSubjectIdentityAndRotatesPresentedCredential() {
        TeacherAccount teacher = teacher(TeacherAccountStatus.ACTIVE);
        ExternalIdentityLink link = identityLink();
        when(teacherIdentities.findExternalIdentity(ISSUER, SUBJECT)).thenReturn(Optional.of(link));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID)).thenReturn(Optional.of(teacher));
        tokenGenerator.add(REPLACEMENT_SESSION_TOKEN, REPLACEMENT_CSRF_TOKEN);

        EstablishedTeacherSession established = service.establish(
                new ExternalTeacherIdentity(ISSUER, SUBJECT), Optional.of(SESSION_TOKEN));

        ArgumentCaptor<TeacherBrowserSession> sessionCaptor =
                ArgumentCaptor.forClass(TeacherBrowserSession.class);
        verify(browserSessions).createReplacing(
                sessionCaptor.capture(), eq(Optional.of(verifier.verifierFor(SESSION_TOKEN))), eq(NOW));
        TeacherBrowserSession stored = sessionCaptor.getValue();
        assertThat(established.sessionCredential().expose())
                .isEqualTo(REPLACEMENT_SESSION_TOKEN.expose())
                .isNotEqualTo(SESSION_TOKEN.expose());
        assertThat(established.csrfToken().expose()).isEqualTo(REPLACEMENT_CSRF_TOKEN.expose());
        assertThat(stored.teacherAccountId()).isEqualTo(TEACHER_ID);
        assertThat(stored.credentialVerifier()).isEqualTo(verifier.verifierFor(REPLACEMENT_SESSION_TOKEN));
        assertThat(stored.csrfVerifier()).isEqualTo(verifier.verifierFor(REPLACEMENT_CSRF_TOKEN));
        assertThat(stored.toString())
                .doesNotContain(REPLACEMENT_SESSION_TOKEN.expose())
                .doesNotContain(
                        REPLACEMENT_CSRF_TOKEN.expose(),
                        stored.credentialVerifier(),
                        stored.csrfVerifier())
                .contains("REDACTED");
        assertThat(stored.createdAt()).isEqualTo(NOW);
        assertThat(stored.lastActiveAt()).isEqualTo(NOW);
        assertThat(stored.idleExpiresAt()).isEqualTo(NOW.plusSeconds(30 * 60));
        assertThat(stored.absoluteExpiresAt()).isEqualTo(NOW.plusSeconds(8 * 60 * 60));
        assertThat(established.absoluteExpiresAt()).isEqualTo(stored.absoluteExpiresAt());
        assertThat(established.toString())
                .doesNotContain(REPLACEMENT_SESSION_TOKEN.expose())
                .doesNotContain(REPLACEMENT_CSRF_TOKEN.expose());
    }

    @Test
    void rejectsUnknownIdentityWithoutProvisioningOrSessionCreation() {
        when(teacherIdentities.findExternalIdentity(ISSUER, SUBJECT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.establish(
                        new ExternalTeacherIdentity(ISSUER, SUBJECT), Optional.empty()))
                .isInstanceOf(TeacherAuthenticationRejectedException.class);

        verify(teacherIdentities, never()).findTeacherAccount(any());
        verify(browserSessions, never()).createReplacing(any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = TeacherAccountStatus.class, names = {"DISABLED", "DELETION_REQUESTED", "CLOSED"})
    void rejectsEveryInactiveTeacherAccount(TeacherAccountStatus status) {
        when(teacherIdentities.findExternalIdentity(ISSUER, SUBJECT))
                .thenReturn(Optional.of(identityLink()));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(teacher(status)));

        assertThatThrownBy(() -> service.establish(
                        new ExternalTeacherIdentity(ISSUER, SUBJECT), Optional.empty()))
                .isInstanceOf(TeacherAuthenticationRejectedException.class);

        verify(browserSessions, never()).createReplacing(any(), any(), any());
    }

    @Test
    void authenticatesUsableSessionWithoutRefreshingActivity() {
        TeacherBrowserSession session = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        when(browserSessions.findByCredentialVerifier(verifier.verifierFor(SESSION_TOKEN)))
                .thenReturn(Optional.of(session));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(teacher(TeacherAccountStatus.ACTIVE)));

        assertThat(service.authenticate(SESSION_TOKEN))
                .contains(new AuthenticatedTeacherSession(teacher(TeacherAccountStatus.ACTIVE), session));
        verify(browserSessions, never()).refreshActivity(any(), any(), any());
    }

    @Test
    void rejectsRevokedIdleBoundaryAbsoluteBoundaryAndInactiveAccountSessions() {
        assertAuthenticationRejected(session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), NOW.minusSeconds(1)));
        assertAuthenticationRejected(session(
                NOW.minusSeconds(60), NOW, NOW.plusSeconds(3600), null));
        assertAuthenticationRejected(session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW, null));

        TeacherBrowserSession usable = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        when(browserSessions.findByCredentialVerifier(verifier.verifierFor(SESSION_TOKEN)))
                .thenReturn(Optional.of(usable));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(teacher(TeacherAccountStatus.DISABLED)));
        assertThat(service.authenticate(SESSION_TOKEN)).isEmpty();
    }

    @Test
    void bootstrapReturnsStableValidCsrfAndRefreshesIdleWithoutChangingAbsoluteExpiry() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(20 * 60), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));
        when(browserSessions.refreshActivity(
                        SESSION_ID, NOW, current.absoluteExpiresAt()))
                .thenReturn(true);

        TeacherSessionBootstrap bootstrap = service.bootstrap(authenticated, Optional.of(CSRF_TOKEN));

        assertThat(bootstrap.csrfToken().expose()).isEqualTo(CSRF_TOKEN.expose());
        assertThat(bootstrap.recoveryCookieReplaced()).isFalse();
        assertThat(bootstrap.absoluteExpiresAt()).isEqualTo(current.absoluteExpiresAt());
        verify(browserSessions, never()).replaceCsrfVerifierAndRefreshActivity(
                any(), any(), any(), any(), any());
    }

    @Test
    void bootstrapRegeneratesMissingOrMismatchedRecoveryCookieWithVerifierCompareAndSet() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));
        tokenGenerator.add(REPLACEMENT_CSRF_TOKEN);
        when(browserSessions.replaceCsrfVerifierAndRefreshActivity(
                        SESSION_ID,
                        current.csrfVerifier(),
                        verifier.verifierFor(REPLACEMENT_CSRF_TOKEN),
                        NOW,
                        NOW.plusSeconds(30 * 60)))
                .thenReturn(true);

        TeacherSessionBootstrap bootstrap = service.bootstrap(
                authenticated, Optional.of(token('W')));

        assertThat(bootstrap.csrfToken().expose()).isEqualTo(REPLACEMENT_CSRF_TOKEN.expose());
        assertThat(bootstrap.recoveryCookieReplaced()).isTrue();
        assertThat(bootstrap.toString()).doesNotContain(REPLACEMENT_CSRF_TOKEN.expose());
    }

    @Test
    void concurrentCsrfRecoveryFailureRejectsTheSession() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));
        tokenGenerator.add(REPLACEMENT_CSRF_TOKEN);

        assertThatThrownBy(() -> service.bootstrap(authenticated, Optional.empty()))
                .isInstanceOf(TeacherSessionRequiredException.class);
    }

    @Test
    void csrfVerificationIsSessionSpecificAndMissingMaterialFails() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));

        assertThat(service.verify(authenticated, Optional.of(CSRF_TOKEN))).isTrue();
        assertThat(service.verify(authenticated, Optional.of(token('X')))).isFalse();
        assertThat(service.verify(authenticated, Optional.of(SESSION_TOKEN))).isFalse();
        assertThat(service.verify(authenticated, Optional.empty())).isFalse();
    }

    @Test
    void authorizesTeacherMutationOnlyAfterCurrentSessionCsrfAndActivityRefresh() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));
        when(browserSessions.refreshActivity(
                        SESSION_ID, NOW, NOW.plusSeconds(30 * 60)))
                .thenReturn(true);

        assertThat(service.authorizeMutation(authenticated, Optional.of(CSRF_TOKEN)).teacherAccountId())
                .isEqualTo(TEACHER_ID.value());
    }

    @Test
    void recordsAuthorizedReadActivityWithTheSlidingIdleTimeout() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));
        when(browserSessions.refreshActivity(
                        SESSION_ID, NOW, NOW.plusSeconds(30 * 60)))
                .thenReturn(true);

        var authorized = service.authorizeRead(authenticated);
        service.recordActivity(authorized);

        assertThat(authorized.teacherAccountId()).isEqualTo(TEACHER_ID.value());
        assertThat(authorized.teacherBrowserSessionId()).isEqualTo(SESSION_ID.value());
        verify(browserSessions).refreshActivity(
                SESSION_ID, NOW, NOW.plusSeconds(30 * 60));
    }

    @Test
    void capsAuthorizedReadActivityAtTheFixedAbsoluteExpiry() {
        Instant absoluteExpiry = NOW.plusSeconds(10 * 60);
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), absoluteExpiry, null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));
        when(browserSessions.refreshActivity(SESSION_ID, NOW, absoluteExpiry))
                .thenReturn(true);

        service.recordActivity(new AuthorizedTeacherSession(
                TEACHER_ID.value(), SESSION_ID.value()));

        verify(browserSessions).refreshActivity(SESSION_ID, NOW, absoluteExpiry);
    }

    @Test
    void rejectsMissingWrongOrSessionCredentialCsrfWithoutRefreshingActivity() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));

        assertThatThrownBy(() -> service.authorizeMutation(authenticated, Optional.empty()))
                .isInstanceOf(TeacherCsrfRejectedException.class);
        assertThatThrownBy(() -> service.authorizeMutation(authenticated, Optional.of(token('X'))))
                .isInstanceOf(TeacherCsrfRejectedException.class);
        assertThatThrownBy(() -> service.authorizeMutation(authenticated, Optional.of(SESSION_TOKEN)))
                .isInstanceOf(TeacherCsrfRejectedException.class);

        verify(browserSessions, never()).refreshActivity(any(), any(), any());
    }

    @Test
    void treatsActivityRefreshRaceAsSessionRequired() {
        TeacherBrowserSession current = session(
                NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(3600), null);
        AuthenticatedTeacherSession authenticated = authenticated(current);
        when(browserSessions.findById(SESSION_ID)).thenReturn(Optional.of(current));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(authenticated.teacherAccount()));

        assertThatThrownBy(() -> service.authorizeMutation(authenticated, Optional.of(CSRF_TOKEN)))
                .isInstanceOf(TeacherSessionRequiredException.class);
    }

    @Test
    void revocationUsesTheInjectedClock() {
        when(browserSessions.revoke(SESSION_ID, NOW)).thenReturn(true);

        assertThat(service.revoke(SESSION_ID)).isTrue();
    }

    private void assertAuthenticationRejected(TeacherBrowserSession session) {
        when(browserSessions.findByCredentialVerifier(verifier.verifierFor(SESSION_TOKEN)))
                .thenReturn(Optional.of(session));
        assertThat(service.authenticate(SESSION_TOKEN)).isEmpty();
    }

    private AuthenticatedTeacherSession authenticated(TeacherBrowserSession session) {
        return new AuthenticatedTeacherSession(teacher(TeacherAccountStatus.ACTIVE), session);
    }

    private TeacherBrowserSession session(
            Instant lastActiveAt,
            Instant idleExpiresAt,
            Instant absoluteExpiresAt,
            Instant revokedAt) {
        return new TeacherBrowserSession(
                SESSION_ID,
                TEACHER_ID,
                verifier.verifierFor(SESSION_TOKEN),
                verifier.verifierFor(CSRF_TOKEN),
                NOW.minusSeconds(120),
                lastActiveAt,
                idleExpiresAt,
                absoluteExpiresAt,
                revokedAt);
    }

    private static TeacherAccount teacher(TeacherAccountStatus status) {
        return new TeacherAccount(TEACHER_ID, status, NOW.minusSeconds(3600));
    }

    private static ExternalIdentityLink identityLink() {
        return new ExternalIdentityLink(
                new ExternalIdentityLinkId(UUID.fromString("10000000-0000-0000-0000-000000000103")),
                TEACHER_ID,
                ISSUER,
                SUBJECT,
                NOW.minusSeconds(1800));
    }

    private static RawSecurityToken token(char character) {
        return RawSecurityToken.fromEncoded(String.valueOf(character).repeat(43));
    }

    private static final class QueueTokenGenerator implements SecurityTokenGeneratorPort {

        private final Queue<RawSecurityToken> tokens = new ArrayDeque<>();

        void add(RawSecurityToken... additions) {
            tokens.addAll(java.util.List.of(additions));
        }

        @Override
        public RawSecurityToken generate() {
            return tokens.remove();
        }
    }
}
