package io.github.sipratama.penatika.classroom.application.port.in;

import io.github.sipratama.penatika.classroom.application.model.StartClassroomSessionCommand;
import io.github.sipratama.penatika.classroom.application.model.StartedClassroomSession;

public interface StartClassroomSessionUseCase {

    StartedClassroomSession start(StartClassroomSessionCommand command);
}
