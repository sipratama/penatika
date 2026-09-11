package io.github.sipratama.penatika.classroom.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
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
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.model.ResolvedParticipantSession;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;
import io.github.sipratama.penatika.lesson.application.model.ClassroomLessonScene;
import io.github.sipratama.penatika.lesson.application.model.ClassroomLessonSceneBlock;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveClassroomLessonSceneUseCase;

@ExtendWith(MockitoExtension.class)
class ClassroomDisplaySnapshotApplicationServiceTest {

    private static final UUID SESSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000901");
    private static final UUID OTHER_SESSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000999");
    private static final UUID VERSION_ID = UUID.fromString("20000000-0000-0000-0000-000000000901");
    private static final RawSecurityToken TOKEN = RawSecurityToken.fromEncoded("d".repeat(43));

    @Mock private ParticipantSessionAuthorityUseCase participantSessions;
    @Mock private ClassroomSessionPersistencePort classroomSessions;
    @Mock private ResolveClassroomLessonSceneUseCase lessonScenes;

    private ClassroomDisplaySnapshotApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ClassroomDisplaySnapshotApplicationService(
                participantSessions, classroomSessions, lessonScenes);
    }

    @Test
    void derivesClosedCurrentProjectionAfterParticipantRoleAndBindingAuthorization() {
        ClassroomSession session = session(ClassroomLifecycleState.ACTIVE);
        when(participantSessions.resolve(TOKEN)).thenReturn(Optional.of(display(SESSION_ID)));
        when(classroomSessions.findById(session.id())).thenReturn(Optional.of(session));
        when(lessonScenes.resolveScene(VERSION_ID, 1)).thenReturn(Optional.of(new ClassroomLessonScene(
                "20000000-0000-0000-0000-000000000911",
                1,
                List.of(
                        new ClassroomLessonSceneBlock("PLAIN_TEXT", "A < B & C \uD83E\uDDEE"),
                        new ClassroomLessonSceneBlock("PLAIN_TEXT", "second line\nordered")))));

        var projection = service.getSnapshot(SESSION_ID.toString(), Optional.of(TOKEN));

        assertThat(projection.schemaVersion()).isEqualTo("1.0");
        assertThat(projection.classroomSessionId()).isEqualTo(SESSION_ID.toString());
        assertThat(projection.revision()).isEqualTo(4);
        assertThat(projection.scene().position()).isEqualTo(1);
        assertThat(projection.scene().blocks())
                .extracting(block -> block.text())
                .containsExactly("A < B & C \uD83E\uDDEE", "second line\nordered");

        InOrder order = inOrder(participantSessions, classroomSessions, lessonScenes);
        order.verify(participantSessions).resolve(TOKEN);
        order.verify(classroomSessions).findById(session.id());
        order.verify(lessonScenes).resolveScene(VERSION_ID, 1);
        verify(classroomSessions, never()).lockById(any());
    }

    @Test
    void missingOrUnusableCredentialFailsBeforeAnyClassroomLookup() {
        assertThatThrownBy(() -> service.getSnapshot(SESSION_ID.toString(), Optional.empty()))
                .isInstanceOf(DisplaySessionRequiredException.class);
        when(participantSessions.resolve(TOKEN)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getSnapshot(SESSION_ID.toString(), Optional.of(TOKEN)))
                .isInstanceOf(DisplaySessionRequiredException.class);

        verify(classroomSessions, never()).findById(any());
        verify(classroomSessions, never()).lockById(any());
    }

    @Test
    void controllerRoleFailsWithoutDisclosingRoleAndBeforeClassroomLookup() {
        when(participantSessions.resolve(TOKEN)).thenReturn(Optional.of(controller()));

        assertThatThrownBy(() -> service.getSnapshot(SESSION_ID.toString(), Optional.of(TOKEN)))
                .isInstanceOf(DisplayAuthorityRequiredException.class);

        verify(classroomSessions, never()).findById(any());
    }

    @Test
    void opaqueNonUuidAndForeignBindingAreBothNonDisclosingNotFoundAfterDisplayAuthority() {
        when(participantSessions.resolve(TOKEN)).thenReturn(Optional.of(display(SESSION_ID)));

        assertThatThrownBy(() -> service.getSnapshot("opaque-id", Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);
        assertThatThrownBy(() -> service.getSnapshot("1-1-1-1-1", Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);
        assertThatThrownBy(() -> service.getSnapshot(OTHER_SESSION_ID.toString(), Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);

        verify(classroomSessions, never()).findById(any());
    }

    @Test
    void inaccessibleOrMissingClassroomAndMissingExactSceneFailClosed() {
        ClassroomSession failed = session(ClassroomLifecycleState.FAILED);
        when(participantSessions.resolve(TOKEN)).thenReturn(Optional.of(display(SESSION_ID)));
        when(classroomSessions.findById(failed.id()))
                .thenReturn(Optional.empty(), Optional.of(failed), Optional.of(session(ClassroomLifecycleState.READY)));
        when(lessonScenes.resolveScene(VERSION_ID, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSnapshot(SESSION_ID.toString(), Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);
        assertThatThrownBy(() -> service.getSnapshot(SESSION_ID.toString(), Optional.of(TOKEN)))
                .isInstanceOf(ClassroomSessionNotFoundException.class);
        assertThatThrownBy(() -> service.getSnapshot(SESSION_ID.toString(), Optional.of(TOKEN)))
                .isInstanceOf(IllegalStateException.class);
    }

    private static ClassroomSession session(ClassroomLifecycleState lifecycle) {
        return new ClassroomSession(
                new ClassroomSessionId(SESSION_ID),
                UUID.fromString("10000000-0000-0000-0000-000000000901"),
                VERSION_ID,
                lifecycle,
                1,
                new Revision(4),
                Instant.parse("2026-09-11T08:00:00Z"));
    }

    private static ResolvedParticipantSession display(UUID classroomSessionId) {
        return participant(classroomSessionId, "CLASSROOM_DISPLAY");
    }

    private static ResolvedParticipantSession controller() {
        return participant(SESSION_ID, "TEACHER_CONTROLLER");
    }

    private static ResolvedParticipantSession participant(UUID classroomSessionId, String role) {
        return new ResolvedParticipantSession(
                UUID.fromString("10000000-0000-0000-0000-000000000903"),
                classroomSessionId,
                role,
                null,
                null,
                Instant.parse("2026-09-11T07:00:00Z"),
                Instant.parse("2026-09-11T15:00:00Z"));
    }
}
