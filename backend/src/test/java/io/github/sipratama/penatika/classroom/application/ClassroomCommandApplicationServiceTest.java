package io.github.sipratama.penatika.classroom.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandRequest;
import io.github.sipratama.penatika.classroom.application.port.out.AcceptedCommandOutcomePersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.DisplayMutationGatePort;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcome;
import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveNextLessonSceneUseCase;

@ExtendWith(MockitoExtension.class)
class ClassroomCommandApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-11T08:00:00Z");
    private static final UUID SESSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID TEACHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BROWSER_ID = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID PARTICIPANT_ID = UUID.fromString("10000000-0000-0000-0000-000000000004");
    private static final UUID LESSON_VERSION_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final RawSecurityToken PARTICIPANT_TOKEN =
            RawSecurityToken.fromEncoded("p".repeat(43));

    @Mock private ClassroomSessionPersistencePort classroomSessions;
    @Mock private AcceptedCommandOutcomePersistencePort acceptedCommands;
    @Mock private DisplayMutationGatePort displayGate;
    @Mock private ResolveNextLessonSceneUseCase lessonNavigation;
    @Mock private ParticipantSessionAuthorityUseCase participantSessions;

    private ClassroomCommandApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ClassroomCommandApplicationService(
                classroomSessions,
                acceptedCommands,
                displayGate,
                lessonNavigation,
                new ControllerAuthorityApplicationService(participantSessions),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void acceptsNextAndPersistsTheOriginalAuthorityContext() {
        ClassroomSession session = session(0, 0, ClassroomLifecycleState.CREATED);
        authorize(session);
        when(acceptedCommands.findByCommandIdentity(session.id(), "command-1"))
                .thenReturn(Optional.empty());
        when(lessonNavigation.resolveNextScenePosition(LESSON_VERSION_ID, 0))
                .thenReturn(OptionalLong.of(1));
        when(displayGate.isMutationPermitted(session.id(), new Revision(0))).thenReturn(true);
        when(classroomSessions.updatePositionIfRevisionMatches(
                        session.id(), new Revision(0), 1, new Revision(1), ClassroomLifecycleState.CREATED))
                .thenReturn(true);
        when(acceptedCommands.saveIfAbsent(any())).thenReturn(true);

        var execution = service.execute(request("command-1", 0));
        var result = execution.result();

        assertThat(execution.newlyAccepted()).isTrue();
        assertThat(result.classroomSessionId()).isEqualTo(SESSION_ID.toString());
        assertThat(result.commandId()).isEqualTo("command-1");
        assertThat(result.resultingRevision()).isEqualTo(1);
        ArgumentCaptor<AcceptedCommandOutcome> outcome = ArgumentCaptor.forClass(AcceptedCommandOutcome.class);
        verify(acceptedCommands).saveIfAbsent(outcome.capture());
        assertThat(outcome.getValue().teacherAccountId()).isEqualTo(TEACHER_ID);
        assertThat(outcome.getValue().teacherBrowserSessionId()).isEqualTo(BROWSER_ID);
        assertThat(outcome.getValue().controllerParticipantSessionId()).isEqualTo(PARTICIPANT_ID);
        assertThat(outcome.getValue().acceptedAt()).isEqualTo(NOW);
        verify(classroomSessions).lockById(session.id());
    }

    @Test
    void equivalentReplayReturnsOriginalOutcomeWithoutCurrentRevisionNavigationOrGate() {
        ClassroomSession current = session(2, 2, ClassroomLifecycleState.FAILED);
        authorize(current);
        AcceptedCommandOutcome original = outcome("command-1", 0, 1);
        when(acceptedCommands.findByCommandIdentity(current.id(), "command-1"))
                .thenReturn(Optional.of(original));

        var execution = service.execute(request("command-1", 0));

        assertThat(execution.newlyAccepted()).isFalse();
        assertThat(execution.result().resultingRevision()).isEqualTo(1);
        verify(displayGate, never()).isMutationPermitted(any(), any());
        verify(lessonNavigation, never()).resolveNextScenePosition(any(), any(Long.class));
        verify(classroomSessions, never()).updatePositionIfRevisionMatches(any(), any(), any(Long.class), any(), any());
    }

    @Test
    void changedAcceptedReuseConflictsWithoutMutation() {
        ClassroomSession session = session(1, 1, ClassroomLifecycleState.READY);
        authorize(session);
        when(acceptedCommands.findByCommandIdentity(session.id(), "command-1"))
                .thenReturn(Optional.of(outcome("command-1", 0, 1)));

        assertThatThrownBy(() -> service.execute(request("command-1", 1)))
                .isInstanceOf(CommandIdReuseConflictException.class);
        verify(classroomSessions, never()).updatePositionIfRevisionMatches(any(), any(), any(Long.class), any(), any());
    }

    @Test
    void unseenStaleClosedFinalTerminalAndMaxRevisionCommandsDoNotReserveIdentity() {
        assertRejected(session(1, 1, ClassroomLifecycleState.READY), 0, StaleRevisionException.class, true, true);
        assertRejected(session(0, 0, ClassroomLifecycleState.READY), 0, ClassroomMutationNotAllowedException.class, false, true);
        assertRejected(session(1, 0, ClassroomLifecycleState.READY), 0, ClassroomMutationNotAllowedException.class, true, false);
        assertRejected(session(0, 0, ClassroomLifecycleState.EXPIRED), 0, ClassroomMutationNotAllowedException.class, true, true);
        assertRejected(
                session(0, Revision.MAX_VALUE, ClassroomLifecycleState.ACTIVE),
                Revision.MAX_VALUE,
                ClassroomMutationNotAllowedException.class,
                true,
                true);
    }

    @Test
    void wrongRoleOrBindingIsOneControllerAuthorityFailure() {
        ClassroomSession session = session(0, 0, ClassroomLifecycleState.READY);
        when(classroomSessions.findById(session.id())).thenReturn(Optional.of(session));
        when(participantSessions.resolve(PARTICIPANT_TOKEN)).thenReturn(Optional.of(
                new ResolvedParticipantSession(
                        PARTICIPANT_ID,
                        SESSION_ID,
                        "CLASSROOM_DISPLAY",
                        null,
                        null,
                        NOW.minusSeconds(1),
                        NOW.plusSeconds(1))));

        assertThatThrownBy(() -> service.execute(request("command-1", 0)))
                .isInstanceOf(ControllerAuthorityRequiredException.class);
        verify(classroomSessions, never()).lockById(any());
        verify(acceptedCommands, never()).findByCommandIdentity(any(), any());
    }

    @Test
    void rejectsAnotherTeachersClassroomBeforeAcquiringTheMutationRowLock() {
        ClassroomSession anotherTeachersSession = session(
                UUID.fromString("10000000-0000-0000-0000-000000000099"),
                0,
                0,
                ClassroomLifecycleState.READY);
        when(classroomSessions.findById(anotherTeachersSession.id()))
                .thenReturn(Optional.of(anotherTeachersSession));

        assertThatThrownBy(() -> service.execute(request("command-1", 0)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);

        verify(classroomSessions, never()).lockById(any());
        verify(participantSessions, never()).resolve(any());
    }

    @Test
    void preauthorizesBeforeLockAndRevalidatesControllerAuthorityAfterLock() {
        ClassroomSession session = session(0, 0, ClassroomLifecycleState.READY);
        when(classroomSessions.findById(session.id())).thenReturn(Optional.of(session));
        when(classroomSessions.lockById(session.id())).thenReturn(Optional.of(session));
        when(participantSessions.resolve(PARTICIPANT_TOKEN))
                .thenReturn(Optional.of(authorizedParticipant()), Optional.empty());

        assertThatThrownBy(() -> service.execute(request("command-1", 0)))
                .isInstanceOf(ControllerAuthorityRequiredException.class);

        InOrder authorizationOrder = inOrder(classroomSessions, participantSessions);
        authorizationOrder.verify(classroomSessions).findById(session.id());
        authorizationOrder.verify(participantSessions).resolve(PARTICIPANT_TOKEN);
        authorizationOrder.verify(classroomSessions).lockById(session.id());
        authorizationOrder.verify(participantSessions).resolve(PARTICIPANT_TOKEN);
        verify(acceptedCommands, never()).findByCommandIdentity(any(), any());
    }

    @Test
    void revalidatesClassroomOwnershipAfterAcquiringTheMutationRowLock() {
        ClassroomSession preauthorized = session(0, 0, ClassroomLifecycleState.READY);
        ClassroomSession ownershipChanged = session(
                UUID.fromString("10000000-0000-0000-0000-000000000099"),
                0,
                0,
                ClassroomLifecycleState.READY);
        when(classroomSessions.findById(preauthorized.id())).thenReturn(Optional.of(preauthorized));
        when(classroomSessions.lockById(preauthorized.id())).thenReturn(Optional.of(ownershipChanged));
        when(participantSessions.resolve(PARTICIPANT_TOKEN))
                .thenReturn(Optional.of(authorizedParticipant()));

        assertThatThrownBy(() -> service.execute(request("command-1", 0)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);

        verify(participantSessions).resolve(PARTICIPANT_TOKEN);
        verify(acceptedCommands, never()).findByCommandIdentity(any(), any());
    }

    private void assertRejected(
            ClassroomSession session,
            long expectedRevision,
            Class<? extends RuntimeException> exceptionType,
            boolean nextSceneExists,
            boolean gateOpen) {
        org.mockito.Mockito.reset(
                classroomSessions, acceptedCommands, displayGate, lessonNavigation, participantSessions);
        authorize(session);
        when(acceptedCommands.findByCommandIdentity(session.id(), "command-1"))
                .thenReturn(Optional.empty());
        if (session.permitsStudentFacingMutation()
                && session.revision().value() != Revision.MAX_VALUE
                && session.revision().value() == expectedRevision) {
            when(lessonNavigation.resolveNextScenePosition(
                            session.lessonVersionId(), session.currentScenePosition()))
                    .thenReturn(nextSceneExists ? OptionalLong.of(session.currentScenePosition() + 1) : OptionalLong.empty());
            if (nextSceneExists) {
                when(displayGate.isMutationPermitted(session.id(), session.revision())).thenReturn(gateOpen);
            }
        }

        assertThatThrownBy(() -> service.execute(request("command-1", expectedRevision)))
                .isInstanceOf(exceptionType);
        verify(acceptedCommands, never()).saveIfAbsent(any());
    }

    private void authorize(ClassroomSession session) {
        when(classroomSessions.findById(session.id())).thenReturn(Optional.of(session));
        when(classroomSessions.lockById(session.id())).thenReturn(Optional.of(session));
        when(participantSessions.resolve(PARTICIPANT_TOKEN))
                .thenReturn(Optional.of(authorizedParticipant()));
    }

    private static ResolvedParticipantSession authorizedParticipant() {
        return new ResolvedParticipantSession(
                PARTICIPANT_ID,
                SESSION_ID,
                "TEACHER_CONTROLLER",
                TEACHER_ID,
                BROWSER_ID,
                NOW.minusSeconds(1),
                NOW.plusSeconds(1));
    }

    private static ClassroomCommandRequest request(String commandId, long revision) {
        return new ClassroomCommandRequest(
                SESSION_ID,
                commandId,
                revision,
                TEACHER_ID,
                BROWSER_ID,
                Optional.of(PARTICIPANT_TOKEN));
    }

    private static ClassroomSession session(
            long position, long revision, ClassroomLifecycleState lifecycle) {
        return session(TEACHER_ID, position, revision, lifecycle);
    }

    private static ClassroomSession session(
            UUID teacherAccountId,
            long position,
            long revision,
            ClassroomLifecycleState lifecycle) {
        return new ClassroomSession(
                new ClassroomSessionId(SESSION_ID),
                teacherAccountId,
                LESSON_VERSION_ID,
                lifecycle,
                position,
                new Revision(revision),
                NOW.minusSeconds(60));
    }

    private static AcceptedCommandOutcome outcome(String commandId, long expected, long resulting) {
        return new AcceptedCommandOutcome(
                new io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcomeId(UUID.randomUUID()),
                new ClassroomSessionId(SESSION_ID),
                commandId,
                new Revision(expected),
                io.github.sipratama.penatika.classroom.domain.CommandType.DIRECT_ACTION,
                io.github.sipratama.penatika.classroom.domain.ClassroomAction.NEXT,
                TEACHER_ID,
                BROWSER_ID,
                PARTICIPANT_ID,
                new Revision(resulting),
                NOW);
    }
}
