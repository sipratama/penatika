package io.github.sipratama.penatika.classroom.application;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

import io.github.sipratama.penatika.classroom.application.model.StartClassroomSessionCommand;
import io.github.sipratama.penatika.classroom.application.model.StartedClassroomSession;
import io.github.sipratama.penatika.classroom.application.port.in.StartClassroomSessionUseCase;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;

public final class StartClassroomSessionApplicationService implements StartClassroomSessionUseCase {

    private final ClassroomSessionPersistencePort classroomSessions;
    private final Clock clock;

    public StartClassroomSessionApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            Clock clock) {
        this.classroomSessions = Objects.requireNonNull(classroomSessions, "classroomSessions must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public StartedClassroomSession start(StartClassroomSessionCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        ClassroomSession session = ClassroomSession.start(
                new ClassroomSessionId(UUID.randomUUID()),
                command.teacherAccountId(),
                command.lessonVersionId(),
                clock.instant());
        classroomSessions.create(session);
        return new StartedClassroomSession(
                session.id().value().toString(),
                command.externalLessonVersionId(),
                session.revision().value());
    }
}
