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
class ClassroomLessonNavigationApplicationServiceTest {

    private static final UUID VERSION_UUID = UUID.fromString("20000000-0000-0000-0000-000000000801");
    private static final LessonVersionId VERSION_ID = new LessonVersionId(VERSION_UUID);

    @Mock private LessonVersionPersistencePort lessonVersions;

    private ClassroomLessonNavigationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ClassroomLessonNavigationApplicationService(lessonVersions);
    }

    @Test
    void resolvesTheNextSceneFromTheSelectedImmutableLessonVersion() {
        LessonVersion selectedVersion = version(3);
        when(lessonVersions.findById(VERSION_ID)).thenReturn(Optional.of(selectedVersion));

        assertThat(service.resolveNextScenePosition(VERSION_UUID, 0)).hasValue(1);
        assertThat(service.resolveNextScenePosition(VERSION_UUID, 1)).hasValue(2);
        assertThat(selectedVersion).isEqualTo(version(3));
    }

    @Test
    void returnsAbsentAtTheFinalScene() {
        when(lessonVersions.findById(VERSION_ID)).thenReturn(Optional.of(version(3)));

        assertThat(service.resolveNextScenePosition(VERSION_UUID, 2)).isEmpty();
    }

    @Test
    void doesNotNavigateAcrossAnotherOrUnresolvedLessonVersion() {
        UUID otherVersionId = UUID.fromString("20000000-0000-0000-0000-000000000899");
        when(lessonVersions.findById(new LessonVersionId(otherVersionId))).thenReturn(Optional.empty());

        assertThat(service.resolveNextScenePosition(otherVersionId, 0)).isEmpty();
        verify(lessonVersions, never()).findForTeacher(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private static LessonVersion version(int sceneCount) {
        List<LessonScene> scenes = java.util.stream.IntStream.range(0, sceneCount)
                .mapToObj(ClassroomLessonNavigationApplicationServiceTest::scene)
                .toList();
        return new LessonVersion(
                VERSION_ID,
                new LessonId(UUID.fromString("20000000-0000-0000-0000-000000000802")),
                LessonVersionReadiness.CLASSROOM_READY,
                Instant.parse("2026-09-11T08:00:00Z"),
                scenes);
    }

    private static LessonScene scene(int position) {
        LessonSceneId sceneId = new LessonSceneId(new UUID(2, position + 1));
        return new LessonScene(
                sceneId,
                VERSION_ID,
                position,
                List.of(new SceneBlock(
                        new SceneBlockId(new UUID(3, position + 1)),
                        sceneId,
                        0,
                        SceneBlockType.PLAIN_TEXT,
                        "Scene " + position)));
    }
}
