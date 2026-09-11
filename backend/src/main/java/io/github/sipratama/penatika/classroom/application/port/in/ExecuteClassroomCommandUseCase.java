package io.github.sipratama.penatika.classroom.application.port.in;

import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandRequest;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandResult;

public interface ExecuteClassroomCommandUseCase {

    ClassroomCommandResult execute(ClassroomCommandRequest request);
}
