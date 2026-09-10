package io.github.sipratama.penatika.lesson.application;

import java.util.Objects;
import java.util.UUID;

import io.github.sipratama.penatika.lesson.application.model.ClassroomStartLessonVersion;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveClassroomStartLessonVersionUseCase;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;
import io.github.sipratama.penatika.lesson.domain.LessonVersion;
import io.github.sipratama.penatika.lesson.domain.LessonVersionId;

public final class ClassroomStartLessonVersionApplicationService
        implements ResolveClassroomStartLessonVersionUseCase {

    private final LessonVersionPersistencePort lessonVersions;

    public ClassroomStartLessonVersionApplicationService(LessonVersionPersistencePort lessonVersions) {
        this.lessonVersions = Objects.requireNonNull(lessonVersions, "lessonVersions must not be null");
    }

    @Override
    public ClassroomStartLessonVersion resolve(String requestedLessonVersionId, UUID teacherAccountId) {
        Objects.requireNonNull(requestedLessonVersionId, "requestedLessonVersionId must not be null");
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");

        LessonVersionId internalId = parseInternalId(requestedLessonVersionId);
        LessonVersion lessonVersion = lessonVersions.findForTeacher(internalId, teacherAccountId)
                .orElseThrow(LessonVersionNotFoundOrUnauthorizedException::new);
        if (!lessonVersion.isEligibleForClassroomStart()) {
            throw new LessonVersionNotReadyException();
        }
        return new ClassroomStartLessonVersion(requestedLessonVersionId, internalId.value());
    }

    private static LessonVersionId parseInternalId(String requestedLessonVersionId) {
        try {
            UUID parsed = UUID.fromString(requestedLessonVersionId);
            if (!parsed.toString().equalsIgnoreCase(requestedLessonVersionId)) {
                throw new LessonVersionNotFoundOrUnauthorizedException();
            }
            return new LessonVersionId(parsed);
        } catch (IllegalArgumentException exception) {
            throw new LessonVersionNotFoundOrUnauthorizedException();
        }
    }
}
