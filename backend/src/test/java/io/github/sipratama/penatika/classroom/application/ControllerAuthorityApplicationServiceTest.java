package io.github.sipratama.penatika.classroom.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;

@ExtendWith(MockitoExtension.class)
class ControllerAuthorityApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-11T08:00:00Z");
    private static final UUID SESSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID TEACHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BROWSER_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID PARTICIPANT_ID = UUID.fromString("10000000-0000-0000-0000-000000000004");
    private static final RawSecurityToken TOKEN = RawSecurityToken.fromEncoded("c".repeat(43));

    @Mock private ParticipantSessionAuthorityUseCase participantSessions;

    private ControllerAuthorityApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ControllerAuthorityApplicationService(participantSessions);
    }

    @Test
    void acceptsOnlyTheExactControllerSessionTeacherAndBrowserBinding() {
        when(participantSessions.resolve(TOKEN)).thenReturn(Optional.of(resolved(
                "TEACHER_CONTROLLER", SESSION_ID, TEACHER_ID, BROWSER_ID)));

        var authorized = service.requireAuthority(
                session(TEACHER_ID), TEACHER_ID, BROWSER_ID, Optional.of(TOKEN));

        assertThat(authorized.participantSessionId()).isEqualTo(PARTICIPANT_ID);
    }

    @Test
    void missingInvalidDisplayWrongSessionTeacherAndBrowserShareOneFailure() {
        assertThatThrownBy(() -> service.requireAuthority(
                        session(TEACHER_ID), TEACHER_ID, BROWSER_ID, Optional.empty()))
                .isInstanceOf(ControllerAuthorityRequiredException.class);
        for (ResolvedParticipantSession invalid : new ResolvedParticipantSession[] {
                resolved("CLASSROOM_DISPLAY", SESSION_ID, null, null),
                resolved("TEACHER_CONTROLLER", UUID.randomUUID(), TEACHER_ID, BROWSER_ID),
                resolved("TEACHER_CONTROLLER", SESSION_ID, UUID.randomUUID(), BROWSER_ID),
                resolved("TEACHER_CONTROLLER", SESSION_ID, TEACHER_ID, UUID.randomUUID())
        }) {
            when(participantSessions.resolve(TOKEN)).thenReturn(Optional.of(invalid));
            assertThatThrownBy(() -> service.requireAuthority(
                            session(TEACHER_ID), TEACHER_ID, BROWSER_ID, Optional.of(TOKEN)))
                    .isInstanceOf(ControllerAuthorityRequiredException.class);
        }
    }

    @Test
    void anotherTeachersClassroomRemainsNotFoundBeforeParticipantDisclosure() {
        assertThatThrownBy(() -> service.requireAuthority(
                        session(UUID.randomUUID()), TEACHER_ID, BROWSER_ID, Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);
    }

    private static ResolvedParticipantSession resolved(
            String role, UUID sessionId, UUID teacherId, UUID browserId) {
        return new ResolvedParticipantSession(
                PARTICIPANT_ID,
                sessionId,
                role,
                teacherId,
                browserId,
                NOW.minusSeconds(1),
                NOW.plusSeconds(1));
    }

    private static ClassroomSession session(UUID owner) {
        return new ClassroomSession(
                new ClassroomSessionId(SESSION_ID),
                owner,
                UUID.fromString("20000000-0000-0000-0000-000000000002"),
                ClassroomLifecycleState.READY,
                0,
                new Revision(0),
                NOW.minusSeconds(60));
    }
}
