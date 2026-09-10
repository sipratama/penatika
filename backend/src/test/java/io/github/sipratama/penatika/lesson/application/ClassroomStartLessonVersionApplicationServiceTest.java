package io.github.sipratama.penatika.lesson.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.sipratama.penatika.lesson.application.model.ClassroomStartLessonVersion;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;
import io.github.sipratama.penatika.lesson.domain.LessonId;
import io.github.sipratama.penatika.lesson.domain.LessonScene;
import io.github.sipratama.penatika.lesson.domain.LessonSceneId;
import io.github.sipratama.penatika.lesson.domain.LessonVersion;
import io.github.sipratama.penatika.lesson.domain.LessonVersionId;
import io.github.sipratama.penatika.lesson.domain.LessonVersionReadiness;
import io.github.sipratama.penatika.lesson.domain.SceneBlock;
import io.github.sipratama.penatika.lesson.domain.SceneBlockId;
import io.github.sipratama.penatika.lesson.domain.SceneBlockType;

@ExtendWith(MockitoExtension.class)
class ClassroomStartLessonVersionApplicationServiceTest {

    private static final UUID TEACHER_ID = UUID.fromString("10000000-0000-0000-0000-000000000301");
    private static final UUID VERSION_UUID = UUID.fromString("20000000-0000-0000-0000-000000000301");
    private static final String VERSION_ID = VERSION_UUID.toString();
    private static final LessonVersionId INTERNAL_VERSION_ID = new LessonVersionId(VERSION_UUID);

    @Mock private LessonVersionPersistencePort lessonVersions;

    private ClassroomStartLessonVersionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ClassroomStartLessonVersionApplicationService(lessonVersions);
    }

    @Test
    void resolvesOwnedReadyVersionThroughTheTeacherScopedBoundary() {
        when(lessonVersions.findForTeacher(INTERNAL_VERSION_ID, TEACHER_ID))
                .thenReturn(Optional.of(validVersion(LessonVersionReadiness.CLASSROOM_READY)));

        ClassroomStartLessonVersion resolved = service.resolve(VERSION_ID, TEACHER_ID);

        assertThat(resolved.externalId()).isEqualTo(VERSION_ID);
        assertThat(resolved.internalId()).isEqualTo(VERSION_UUID);
        verify(lessonVersions, never()).findById(INTERNAL_VERSION_ID);
    }

    @Test
    void acceptsContiguousScenesAndTheExactDisplayBlockLimit() {
        LessonSceneId firstSceneId = sceneId();
        LessonSceneId secondSceneId = new LessonSceneId(
                UUID.fromString("20000000-0000-0000-0000-000000000304"));
        LessonVersion eligible = version(List.of(
                new LessonScene(firstSceneId, INTERNAL_VERSION_ID, 0, blocks(firstSceneId, 64)),
                new LessonScene(
                        secondSceneId,
                        INTERNAL_VERSION_ID,
                        1,
                        List.of(block(secondSceneId, 0)))));
        when(lessonVersions.findForTeacher(INTERNAL_VERSION_ID, TEACHER_ID))
                .thenReturn(Optional.of(eligible));

        assertThat(service.resolve(VERSION_ID, TEACHER_ID).internalId()).isEqualTo(VERSION_UUID);
    }

    @Test
    void mergesNonexistentAndUnauthorizedVersionsIntoTheSameOutcome() {
        when(lessonVersions.findForTeacher(INTERNAL_VERSION_ID, TEACHER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve(VERSION_ID, TEACHER_ID))
                .isInstanceOf(LessonVersionNotFoundOrUnauthorizedException.class)
                .hasMessageNotContaining(VERSION_ID);
    }

    @Test
    void treatsWireValidNonUuidAsNotFoundWithoutLeakingThePhysicalRepresentation() {
        String opaqueId = "lesson_version_example_01";

        assertThatThrownBy(() -> service.resolve(opaqueId, TEACHER_ID))
                .isInstanceOf(LessonVersionNotFoundOrUnauthorizedException.class)
                .hasMessageNotContaining(opaqueId)
                .hasMessageNotContaining("UUID");

        verify(lessonVersions, never()).findForTeacher(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsOwnedDraftAndCorruptedReadyContentAsNotReady() {
        LessonVersionId otherVersionId = new LessonVersionId(
                UUID.fromString("20000000-0000-0000-0000-000000000399"));
        LessonSceneId otherSceneId = new LessonSceneId(
                UUID.fromString("20000000-0000-0000-0000-000000000398"));
        List<LessonVersion> ineligibleVersions = List.of(
                validVersion(LessonVersionReadiness.DRAFT),
                version(List.of()),
                version(List.of(scene(1, List.of(block(0))))),
                version(List.of(
                        scene(0, List.of(block(0))),
                        new LessonScene(otherSceneId, INTERNAL_VERSION_ID, 2, List.of(block(otherSceneId, 0))))),
                version(List.of(scene(0, List.of()))),
                version(List.of(scene(0, blocks(65)))),
                version(List.of(scene(0, List.of(block(1))))),
                version(List.of(scene(0, List.of(block(0), block(2))))),
                version(List.of(new LessonScene(
                        sceneId(), otherVersionId, 0, List.of(block(0))))),
                version(List.of(new LessonScene(
                        sceneId(), INTERNAL_VERSION_ID, 0, List.of(block(otherSceneId, 0))))));

        for (LessonVersion ineligible : ineligibleVersions) {
            when(lessonVersions.findForTeacher(INTERNAL_VERSION_ID, TEACHER_ID))
                    .thenReturn(Optional.of(ineligible));
            assertThatThrownBy(() -> service.resolve(VERSION_ID, TEACHER_ID))
                    .isInstanceOf(LessonVersionNotReadyException.class);
        }
    }

    private static LessonVersion validVersion(LessonVersionReadiness readiness) {
        LessonScene scene = scene(0, List.of(block(0)));
        return new LessonVersion(
                INTERNAL_VERSION_ID,
                new LessonId(UUID.fromString("20000000-0000-0000-0000-000000000302")),
                readiness,
                Instant.parse("2026-09-11T00:00:00Z"),
                List.of(scene));
    }

    private static LessonVersion version(List<LessonScene> scenes) {
        return new LessonVersion(
                INTERNAL_VERSION_ID,
                new LessonId(UUID.fromString("20000000-0000-0000-0000-000000000302")),
                LessonVersionReadiness.CLASSROOM_READY,
                Instant.parse("2026-09-11T00:00:00Z"),
                scenes);
    }

    private static LessonScene scene(long position, List<SceneBlock> blocks) {
        return new LessonScene(
                sceneId(),
                INTERNAL_VERSION_ID,
                position,
                blocks);
    }

    private static SceneBlock block(long position) {
        return block(sceneId(), position);
    }

    private static SceneBlock block(LessonSceneId parentSceneId, long position) {
        return new SceneBlock(
                new SceneBlockId(new UUID(0, position + 1)),
                parentSceneId,
                position,
                SceneBlockType.PLAIN_TEXT,
                "Classroom-ready plain text");
    }

    private static List<SceneBlock> blocks(int count) {
        return blocks(sceneId(), count);
    }

    private static List<SceneBlock> blocks(LessonSceneId parentSceneId, int count) {
        List<SceneBlock> blocks = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            blocks.add(block(parentSceneId, index));
        }
        return blocks;
    }

    private static LessonSceneId sceneId() {
        return new LessonSceneId(UUID.fromString("20000000-0000-0000-0000-000000000303"));
    }
}
