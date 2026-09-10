package io.github.sipratama.penatika.classroom.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.sipratama.penatika.classroom.application.model.StartClassroomSessionCommand;
import io.github.sipratama.penatika.classroom.application.model.StartedClassroomSession;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.Revision;

@ExtendWith(MockitoExtension.class)
class StartClassroomSessionApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-11T01:00:00Z");
    private static final UUID TEACHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000401");
    private static final UUID LESSON_VERSION_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000401");

    @Mock private ClassroomSessionPersistencePort classroomSessions;

    private StartClassroomSessionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new StartClassroomSessionApplicationService(
                classroomSessions, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsAndPersistsTheLockedInitialAuthoritativeState() {
        StartClassroomSessionCommand command = new StartClassroomSessionCommand(
                TEACHER_ID, LESSON_VERSION_ID, LESSON_VERSION_ID.toString());

        StartedClassroomSession started = service.start(command);

        ArgumentCaptor<ClassroomSession> session = ArgumentCaptor.forClass(ClassroomSession.class);
        verify(classroomSessions).create(session.capture());
        assertThat(session.getValue().teacherAccountId()).isEqualTo(TEACHER_ID);
        assertThat(session.getValue().lessonVersionId()).isEqualTo(LESSON_VERSION_ID);
        assertThat(session.getValue().lifecycleState()).isEqualTo(ClassroomLifecycleState.CREATED);
        assertThat(session.getValue().currentScenePosition()).isZero();
        assertThat(session.getValue().revision()).isEqualTo(new Revision(0));
        assertThat(session.getValue().startedAt()).isEqualTo(NOW);
        assertThat(started.classroomSessionId()).isEqualTo(session.getValue().id().value().toString());
        assertThat(started.lessonVersionId()).isEqualTo(LESSON_VERSION_ID.toString());
        assertThat(started.revision()).isZero();
    }

    @Test
    void separateStartRequestsCreateSeparateClassroomSessions() {
        StartClassroomSessionCommand command = new StartClassroomSessionCommand(
                TEACHER_ID, LESSON_VERSION_ID, LESSON_VERSION_ID.toString());

        StartedClassroomSession first = service.start(command);
        StartedClassroomSession second = service.start(command);

        assertThat(first.classroomSessionId()).isNotEqualTo(second.classroomSessionId());
        verify(classroomSessions, times(2)).create(org.mockito.ArgumentMatchers.any());
    }
}
