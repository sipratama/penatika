package io.github.sipratama.penatika.classroom.application.port.out;

import java.util.Optional;

import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;

public interface ClassroomSessionPersistencePort {

    void create(ClassroomSession classroomSession);

    Optional<ClassroomSession> findById(ClassroomSessionId classroomSessionId);

    Optional<ClassroomSession> lockById(ClassroomSessionId classroomSessionId);

    boolean updatePositionIfRevisionMatches(
            ClassroomSessionId classroomSessionId,
            Revision expectedRevision,
            long newPosition,
            Revision resultingRevision,
            ClassroomLifecycleState lifecycleState);
}
