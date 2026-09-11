package io.github.sipratama.penatika.classroom.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ClassroomSessionTest {

    @Test
    void displayAccessIsLimitedToLiveClassroomLifecycleStates() {
        assertThat(session(ClassroomLifecycleState.CREATED, 0, 0).permitsDisplayAccess()).isTrue();
        assertThat(session(ClassroomLifecycleState.READY, 0, 0).permitsDisplayAccess()).isTrue();
        assertThat(session(ClassroomLifecycleState.ACTIVE, 0, 0).permitsDisplayAccess()).isTrue();
        assertThat(session(ClassroomLifecycleState.FAILED, 0, 0).permitsDisplayAccess()).isFalse();
        assertThat(session(ClassroomLifecycleState.EXPIRED, 0, 0).permitsDisplayAccess()).isFalse();
    }

    private static final ClassroomSessionId SESSION_ID =
            new ClassroomSessionId(UUID.fromString("30000000-0000-0000-0000-000000000701"));
    private static final UUID TEACHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000701");
    private static final UUID LESSON_VERSION_ID = UUID.fromString("20000000-0000-0000-0000-000000000701");
    private static final Instant STARTED_AT = Instant.parse("2026-09-11T08:00:00Z");

    @Test
    void createdReadyAndActiveSessionsPermitProtectedSliceMutation() {
        assertThat(session(ClassroomLifecycleState.CREATED, 0, 0).permitsStudentFacingMutation()).isTrue();
        assertThat(session(ClassroomLifecycleState.READY, 0, 0).permitsStudentFacingMutation()).isTrue();
        assertThat(session(ClassroomLifecycleState.ACTIVE, 0, 0).permitsStudentFacingMutation()).isTrue();
    }

    @Test
    void failedAndExpiredSessionsRejectProtectedSliceMutation() {
        assertThat(session(ClassroomLifecycleState.FAILED, 0, 0).permitsStudentFacingMutation()).isFalse();
        assertThat(session(ClassroomLifecycleState.EXPIRED, 0, 0).permitsStudentFacingMutation()).isFalse();
    }

    @Test
    void nextSceneAdvancesPositionAndRevisionWithoutChangingLifecycleOrReferences() {
        ClassroomSession current = session(ClassroomLifecycleState.CREATED, 0, 7);

        ClassroomSession advanced = current.advanceToNextScene(1);

        assertThat(advanced.id()).isEqualTo(current.id());
        assertThat(advanced.teacherAccountId()).isEqualTo(current.teacherAccountId());
        assertThat(advanced.lessonVersionId()).isEqualTo(current.lessonVersionId());
        assertThat(advanced.lifecycleState()).isEqualTo(ClassroomLifecycleState.CREATED);
        assertThat(advanced.currentScenePosition()).isEqualTo(1);
        assertThat(advanced.revision()).isEqualTo(new Revision(8));
        assertThat(advanced.startedAt()).isEqualTo(current.startedAt());
    }

    @Test
    void nextSceneRejectsNonMutableStateInvalidPositionAndRevisionOverflow() {
        assertThatThrownBy(() -> session(ClassroomLifecycleState.FAILED, 0, 0).advanceToNextScene(1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> session(ClassroomLifecycleState.CREATED, 1, 0).advanceToNextScene(1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> session(
                        ClassroomLifecycleState.CREATED, 0, Revision.MAX_VALUE).advanceToNextScene(1))
                .isInstanceOf(IllegalStateException.class);
    }

    private static ClassroomSession session(
            ClassroomLifecycleState lifecycleState,
            long currentPosition,
            long revision) {
        return new ClassroomSession(
                SESSION_ID,
                TEACHER_ID,
                LESSON_VERSION_ID,
                lifecycleState,
                currentPosition,
                new Revision(revision),
                STARTED_AT);
    }
}
