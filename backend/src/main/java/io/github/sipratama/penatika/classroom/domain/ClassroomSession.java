package io.github.sipratama.penatika.classroom.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ClassroomSession(
        ClassroomSessionId id,
        UUID teacherAccountId,
        UUID lessonVersionId,
        ClassroomLifecycleState lifecycleState,
        long currentScenePosition,
        Revision revision,
        Instant startedAt) {

    public ClassroomSession {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(teacherAccountId, "teacherAccountId must not be null");
        Objects.requireNonNull(lessonVersionId, "lessonVersionId must not be null");
        Objects.requireNonNull(lifecycleState, "lifecycleState must not be null");
        Objects.requireNonNull(revision, "revision must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        if (currentScenePosition < 0 || currentScenePosition > Revision.MAX_VALUE) {
            throw new IllegalArgumentException("currentScenePosition must be within the wire-safe integer range");
        }
    }

    public static ClassroomSession start(
            ClassroomSessionId id,
            UUID teacherAccountId,
            UUID lessonVersionId,
            Instant startedAt) {
        return new ClassroomSession(
                id,
                teacherAccountId,
                lessonVersionId,
                ClassroomLifecycleState.CREATED,
                0,
                new Revision(0),
                startedAt);
    }

    public boolean isPairable() {
        return lifecycleState == ClassroomLifecycleState.CREATED;
    }

    public boolean permitsStudentFacingMutation() {
        return lifecycleState == ClassroomLifecycleState.CREATED
                || lifecycleState == ClassroomLifecycleState.READY
                || lifecycleState == ClassroomLifecycleState.ACTIVE;
    }

    public boolean permitsDisplayAccess() {
        return lifecycleState == ClassroomLifecycleState.CREATED
                || lifecycleState == ClassroomLifecycleState.READY
                || lifecycleState == ClassroomLifecycleState.ACTIVE;
    }

    public ClassroomSession advanceToNextScene(long nextScenePosition) {
        if (!permitsStudentFacingMutation() || revision.value() == Revision.MAX_VALUE) {
            throw new IllegalStateException("ClassroomSession does not permit another mutation");
        }
        if (nextScenePosition <= currentScenePosition || nextScenePosition > Revision.MAX_VALUE) {
            throw new IllegalArgumentException("nextScenePosition must advance within the wire-safe range");
        }
        return new ClassroomSession(
                id,
                teacherAccountId,
                lessonVersionId,
                lifecycleState,
                nextScenePosition,
                new Revision(revision.value() + 1),
                startedAt);
    }
}
