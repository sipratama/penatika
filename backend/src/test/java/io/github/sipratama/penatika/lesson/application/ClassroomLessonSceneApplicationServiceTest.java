package io.github.sipratama.penatika.lesson.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ClassroomLessonSceneApplicationServiceTest {

    private static final UUID VERSION_UUID = UUID.fromString("20000000-0000-0000-0000-000000000901");
    private static final LessonVersionId VERSION_ID = new LessonVersionId(VERSION_UUID);

    @Mock private LessonVersionPersistencePort lessonVersions;

    private ClassroomLessonSceneApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ClassroomLessonSceneApplicationService(lessonVersions);
    }

    @Test
    void resolvesOnlyTheExactSceneFromTheSelectedImmutableVersionAndPreservesBlockOrder() {
        LessonVersion selected = version(VERSION_ID, "first", "middle \uD83E\uDDEE", "last");
        when(lessonVersions.findById(VERSION_ID)).thenReturn(Optional.of(selected));

        var scene = service.resolveScene(VERSION_UUID, 1).orElseThrow();

        assertThat(scene.sceneId()).isEqualTo(selected.scenes().get(1).id().value().toString());
        assertThat(scene.position()).isEqualTo(1);
        assertThat(scene.blocks())
                .extracting(block -> block.plainText())
                .containsExactly("middle \uD83E\uDDEE A", "middle \uD83E\uDDEE B");
        assertThat(scene.blocks())
                .extracting(block -> block.type())
                .containsOnly("PLAIN_TEXT");
    }

    @Test
    void returnsAbsentForMissingVersionOrNonExactPositionWithoutCrossVersionLookup() {
        when(lessonVersions.findById(VERSION_ID)).thenReturn(Optional.of(version(VERSION_ID, "only")));
        UUID otherVersion = UUID.fromString("20000000-0000-0000-0000-000000000999");
        when(lessonVersions.findById(new LessonVersionId(otherVersion))).thenReturn(Optional.empty());

        assertThat(service.resolveScene(VERSION_UUID, 1)).isEmpty();
        assertThat(service.resolveScene(otherVersion, 0)).isEmpty();
        verify(lessonVersions, never()).findForTeacher(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returnsAbsentWhenTheSelectedVersionCannotSatisfyClassroomProjectionInvariants() {
        LessonVersion emptyVersion = new LessonVersion(
                VERSION_ID,
                new LessonId(UUID.fromString("20000000-0000-0000-0000-000000000902")),
                LessonVersionReadiness.CLASSROOM_READY,
                Instant.parse("2026-09-11T08:00:00Z"),
                List.of());
        when(lessonVersions.findById(VERSION_ID)).thenReturn(Optional.of(emptyVersion));

        assertThat(service.resolveScene(VERSION_UUID, 0)).isEmpty();
    }

    private static LessonVersion version(LessonVersionId versionId, String... sceneTexts) {
        List<LessonScene> scenes = java.util.stream.IntStream.range(0, sceneTexts.length)
                .mapToObj(position -> scene(versionId, position, sceneTexts[position]))
                .toList();
        return new LessonVersion(
                versionId,
                new LessonId(UUID.fromString("20000000-0000-0000-0000-000000000902")),
                LessonVersionReadiness.CLASSROOM_READY,
                Instant.parse("2026-09-11T08:00:00Z"),
                scenes);
    }

    private static LessonScene scene(LessonVersionId versionId, int position, String text) {
        LessonSceneId sceneId = new LessonSceneId(new UUID(20, position + 1));
        return new LessonScene(
                sceneId,
                versionId,
                position,
                List.of(
                        new SceneBlock(
                                new SceneBlockId(new UUID(21, position * 2L + 1)),
                                sceneId,
                                0,
                                SceneBlockType.PLAIN_TEXT,
                                text + " A"),
                        new SceneBlock(
                                new SceneBlockId(new UUID(21, position * 2L + 2)),
                                sceneId,
                                1,
                                SceneBlockType.PLAIN_TEXT,
                                text + " B")));
    }
}
