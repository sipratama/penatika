package io.github.sipratama.penatika.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.sipratama.penatika.identity.adapter.out.security.Sha256SecurityTokenVerifier;
import io.github.sipratama.penatika.identity.application.model.EstablishedParticipantSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.out.ParticipantSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenGeneratorPort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;
import io.github.sipratama.penatika.identity.domain.ClassroomSessionReference;
import io.github.sipratama.penatika.identity.domain.ParticipantRole;
import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.ParticipantSessionId;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountStatus;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

@ExtendWith(MockitoExtension.class)
class ParticipantSessionApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-11T04:00:00Z");
    private static final UUID CLASSROOM_ID = UUID.fromString("30000000-0000-0000-0000-000000000501");
    private static final TeacherAccountId TEACHER_ID =
            new TeacherAccountId(UUID.fromString("10000000-0000-0000-0000-000000000501"));
    private static final TeacherBrowserSessionId BROWSER_ID =
            new TeacherBrowserSessionId(UUID.fromString("10000000-0000-0000-0000-000000000502"));
    private static final RawSecurityToken CREDENTIAL = RawSecurityToken.fromEncoded("P".repeat(43));

    @Mock private ParticipantSessionPersistencePort participantSessions;
    @Mock private TeacherIdentityPersistencePort teacherIdentities;
    @Mock private TeacherBrowserSessionPersistencePort browserSessions;
    @Mock private SecurityTokenGeneratorPort tokenGenerator;

    private final Sha256SecurityTokenVerifier verifier = new Sha256SecurityTokenVerifier();
    private ParticipantSessionApplicationService service;

    @BeforeEach
    void setUp() {
        service = serviceAt(NOW);
    }

    @Test
    void createsDisplayWithNewCredentialAndExactEightHourLifetimeWithoutTeacherState() {
        when(participantSessions.findActiveByRole(any(), any())).thenReturn(Optional.empty());
        when(tokenGenerator.generate()).thenReturn(CREDENTIAL);
        when(participantSessions.tryCreateActive(any())).thenReturn(true);

        EstablishedParticipantSession established = service.establishDisplay(CLASSROOM_ID);

        ArgumentCaptor<ParticipantSession> stored = ArgumentCaptor.forClass(ParticipantSession.class);
        verify(participantSessions).tryCreateActive(stored.capture());
        assertThat(stored.getValue().participantRole()).isEqualTo(ParticipantRole.CLASSROOM_DISPLAY);
        assertThat(stored.getValue().teacherAccountId()).isNull();
        assertThat(stored.getValue().teacherBrowserSessionId()).isNull();
        assertThat(stored.getValue().createdAt()).isEqualTo(NOW);
        assertThat(stored.getValue().expiresAt()).isEqualTo(NOW.plusSeconds(8 * 60 * 60));
        assertThat(established.credential()).isSameAs(CREDENTIAL);
        verify(teacherIdentities, never()).findTeacherAccount(any());
        verify(browserSessions, never()).findById(any());
    }

    @Test
    void repeatedResolutionPreservesIdentityAndFixedTimestampsWithoutSlidingUpdates() {
        ParticipantSession persisted = display(NOW.minusSeconds(60), null);
        when(participantSessions.findByCredentialVerifier(verifier.verifierFor(CREDENTIAL)))
                .thenReturn(Optional.of(persisted));

        ResolvedParticipantSession first = service.resolve(CREDENTIAL).orElseThrow();
        ResolvedParticipantSession second = service.resolve(CREDENTIAL).orElseThrow();

        assertThat(second).isEqualTo(first);
        assertThat(first.participantSessionId()).isEqualTo(persisted.id().value());
        assertThat(first.createdAt()).isEqualTo(persisted.createdAt());
        assertThat(first.expiresAt()).isEqualTo(persisted.expiresAt());
        verify(participantSessions, never()).tryCreateActive(any());
        verify(participantSessions, never()).revoke(any(), any());
    }

    @Test
    void exactExpiryAndRevocationBothRejectResolution() {
        ParticipantSession exactExpiry = display(NOW.minusSeconds(8 * 60 * 60), null);
        when(participantSessions.findByCredentialVerifier(verifier.verifierFor(CREDENTIAL)))
                .thenReturn(Optional.of(exactExpiry));
        assertThat(service.resolve(CREDENTIAL)).isEmpty();

        ParticipantSession revoked = display(NOW.minusSeconds(60), NOW.minusSeconds(1));
        when(participantSessions.findByCredentialVerifier(verifier.verifierFor(CREDENTIAL)))
                .thenReturn(Optional.of(revoked));
        assertThat(service.resolve(CREDENTIAL)).isEmpty();
    }

    @Test
    void controllerCreationRequiresCurrentActiveTeacherAndUsableBoundBrowserSession() {
        when(participantSessions.findActiveByRole(any(), any())).thenReturn(Optional.empty());
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(new TeacherAccount(TEACHER_ID, TeacherAccountStatus.ACTIVE, NOW.minusSeconds(1))));
        when(browserSessions.findById(BROWSER_ID)).thenReturn(Optional.of(browserSession()));
        when(tokenGenerator.generate()).thenReturn(CREDENTIAL);
        when(participantSessions.tryCreateActive(any())).thenReturn(true);

        service.establishController(CLASSROOM_ID, TEACHER_ID.value(), BROWSER_ID.value());

        ArgumentCaptor<ParticipantSession> stored = ArgumentCaptor.forClass(ParticipantSession.class);
        verify(participantSessions).tryCreateActive(stored.capture());
        assertThat(stored.getValue().teacherAccountId()).isEqualTo(TEACHER_ID);
        assertThat(stored.getValue().teacherBrowserSessionId()).isEqualTo(BROWSER_ID);
    }

    @Test
    void controllerCreationReportsTeacherSessionRequiredWhenAuthorityDisappears() {
        when(participantSessions.findActiveByRole(any(), any())).thenReturn(Optional.empty());
        when(teacherIdentities.findTeacherAccount(TEACHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.establishController(
                        CLASSROOM_ID, TEACHER_ID.value(), BROWSER_ID.value()))
                .isInstanceOf(TeacherSessionRequiredException.class);
        verify(participantSessions, never()).tryCreateActive(any());
    }

    @Test
    void controllerResolutionRequiresActiveTeacherAndTheOriginallyBoundUsableBrowserSession() {
        ParticipantSession controller = ParticipantSession.controller(
                new ParticipantSessionId(UUID.fromString("10000000-0000-0000-0000-000000000504")),
                new ClassroomSessionReference(CLASSROOM_ID),
                verifier.verifierFor(CREDENTIAL),
                TEACHER_ID,
                BROWSER_ID,
                NOW.minusSeconds(60));
        when(participantSessions.findByCredentialVerifier(verifier.verifierFor(CREDENTIAL)))
                .thenReturn(Optional.of(controller));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(new TeacherAccount(
                        TEACHER_ID, TeacherAccountStatus.ACTIVE, NOW.minusSeconds(3600))));
        when(browserSessions.findById(BROWSER_ID)).thenReturn(Optional.of(browserSession()));

        assertThat(service.resolve(CREDENTIAL)).isPresent();

        when(browserSessions.findById(BROWSER_ID)).thenReturn(Optional.empty());
        assertThat(service.resolve(CREDENTIAL)).isEmpty();
        when(browserSessions.findById(BROWSER_ID)).thenReturn(Optional.of(browserSession()));
        when(teacherIdentities.findTeacherAccount(TEACHER_ID))
                .thenReturn(Optional.of(new TeacherAccount(
                        TEACHER_ID, TeacherAccountStatus.DISABLED, NOW.minusSeconds(3600))));
        assertThat(service.resolve(CREDENTIAL)).isEmpty();
    }

    private ParticipantSessionApplicationService serviceAt(Instant now) {
        return new ParticipantSessionApplicationService(
                participantSessions,
                teacherIdentities,
                browserSessions,
                tokenGenerator,
                verifier,
                Clock.fixed(now, ZoneOffset.UTC));
    }

    private static ParticipantSession display(Instant createdAt, Instant revokedAt) {
        return new ParticipantSession(
                new ParticipantSessionId(UUID.fromString("10000000-0000-0000-0000-000000000503")),
                new ClassroomSessionReference(CLASSROOM_ID),
                ParticipantRole.CLASSROOM_DISPLAY,
                "e".repeat(64),
                null,
                null,
                createdAt,
                createdAt.plusSeconds(8 * 60 * 60),
                revokedAt);
    }

    private static TeacherBrowserSession browserSession() {
        return new TeacherBrowserSession(
                BROWSER_ID,
                TEACHER_ID,
                "f".repeat(64),
                "0".repeat(64),
                NOW.minusSeconds(120),
                NOW.minusSeconds(60),
                NOW.plusSeconds(600),
                NOW.plusSeconds(3600),
                null);
    }
}
