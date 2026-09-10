package io.github.sipratama.penatika.classroom.application.port.out;

import java.util.Optional;

import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcome;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;

public interface AcceptedCommandOutcomePersistencePort {

    boolean saveIfAbsent(AcceptedCommandOutcome outcome);

    Optional<AcceptedCommandOutcome> findByCommandIdentity(
            ClassroomSessionId classroomSessionId, String commandId);
}
