package io.github.sipratama.penatika.lesson.application.port.out;

import java.util.Optional;
import java.util.UUID;

import io.github.sipratama.penatika.lesson.domain.Lesson;
import io.github.sipratama.penatika.lesson.domain.LessonVersion;
import io.github.sipratama.penatika.lesson.domain.LessonVersionId;

public interface LessonVersionPersistencePort {

    void createImmutableVersion(Lesson lesson, LessonVersion lessonVersion);

    Optional<LessonVersion> findById(LessonVersionId lessonVersionId);

    Optional<LessonVersion> findForTeacher(
            LessonVersionId lessonVersionId, UUID teacherAccountId);

    Optional<LessonVersion> findClassroomReadyForTeacher(
            LessonVersionId lessonVersionId, UUID teacherAccountId);
}
