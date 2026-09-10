package io.github.sipratama.penatika.lesson.application.port.in;

import java.util.UUID;

import io.github.sipratama.penatika.lesson.application.model.ClassroomStartLessonVersion;

public interface ResolveClassroomStartLessonVersionUseCase {

    ClassroomStartLessonVersion resolve(String requestedLessonVersionId, UUID teacherAccountId);
}
