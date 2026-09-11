package io.github.sipratama.penatika.classroom.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;
import io.github.sipratama.penatika.identity.application.port.in.RecordTeacherSessionActivityUseCase;

@ExtendWith(MockitoExtension.class)
class ControllerReconciliationApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-11T08:00:00Z");
    private static final UUID SESSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID TEACHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_TEACHER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000099");
    private static final UUID BROWSER_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID PARTICIPANT_ID = UUID.fromString("10000000-0000-0000-0000-000000000004");
    private static final RawSecurityToken TOKEN = RawSecurityToken.fromEncoded("c".repeat(43));
    private static final AuthorizedTeacherSession TEACHER =
            new AuthorizedTeacherSession(TEACHER_ID, BROWSER_ID);

    @Mock private ClassroomSessionPersistencePort classroomSessions;
    @Mock private ParticipantSessionAuthorityUseCase participantSessions;
    @Mock private RecordTeacherSessionActivityUseCase teacherActivity;

    private ControllerReconciliationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ControllerReconciliationApplicationService(
                classroomSessions,
                new ControllerAuthorityApplicationService(participantSessions),
                teacherActivity);
    }

    @Test
    void recordsQualifyingTeacherActivityOnlyAfterCompleteControllerAuthorization() {
        ClassroomSession session = session(TEACHER_ID);
        when(classroomSessions.findById(session.id())).thenReturn(Optional.of(session));
        when(participantSessions.resolve(TOKEN)).thenReturn(Optional.of(controller()));

        var state = service.reconcile(
                SESSION_ID, TEACHER_ID, BROWSER_ID, Optional.of(TOKEN));

        assertThat(state.classroomSessionId()).isEqualTo(SESSION_ID.toString());
        assertThat(state.revision()).isZero();
        InOrder order = inOrder(classroomSessions, participantSessions, teacherActivity);
        order.verify(classroomSessions).findById(session.id());
        order.verify(participantSessions).resolve(TOKEN);
        order.verify(teacherActivity).recordActivity(TEACHER);
    }

    @Test
    void controllerAuthorityFailureDoesNotRecordTeacherActivity() {
        ClassroomSession session = session(TEACHER_ID);
        when(classroomSessions.findById(session.id())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.reconcile(
                        SESSION_ID, TEACHER_ID, BROWSER_ID, Optional.empty()))
                .isInstanceOf(ControllerAuthorityRequiredException.class);

        verify(teacherActivity, never()).recordActivity(TEACHER);
    }

    @Test
    void missingOrAnotherTeachersClassroomDoesNotRecordTeacherActivity() {
        ClassroomSession anotherTeachersSession = session(OTHER_TEACHER_ID);
        when(classroomSessions.findById(anotherTeachersSession.id()))
                .thenReturn(Optional.of(anotherTeachersSession), Optional.empty());

        assertThatThrownBy(() -> service.reconcile(
                        SESSION_ID, TEACHER_ID, BROWSER_ID, Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);
        assertThatThrownBy(() -> service.reconcile(
                        SESSION_ID, TEACHER_ID, BROWSER_ID, Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);

        verify(teacherActivity, never()).recordActivity(TEACHER);
        verify(participantSessions, never()).resolve(TOKEN);
    }

    private static ResolvedParticipantSession controller() {
        return new ResolvedParticipantSession(
                PARTICIPANT_ID,
                SESSION_ID,
                "TEACHER_CONTROLLER",
                TEACHER_ID,
                BROWSER_ID,
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
