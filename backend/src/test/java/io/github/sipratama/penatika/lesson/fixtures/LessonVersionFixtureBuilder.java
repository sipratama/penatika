package io.github.sipratama.penatika.lesson.fixtures;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.github.sipratama.penatika.lesson.domain.Lesson;
import io.github.sipratama.penatika.lesson.domain.LessonId;
import io.github.sipratama.penatika.lesson.domain.LessonScene;
import io.github.sipratama.penatika.lesson.domain.LessonSceneId;
import io.github.sipratama.penatika.lesson.domain.LessonVersion;
import io.github.sipratama.penatika.lesson.domain.LessonVersionId;
import io.github.sipratama.penatika.lesson.domain.LessonVersionReadiness;
import io.github.sipratama.penatika.lesson.domain.SceneBlock;
import io.github.sipratama.penatika.lesson.domain.SceneBlockId;
import io.github.sipratama.penatika.lesson.domain.SceneBlockType;

public final class LessonVersionFixtureBuilder {

    private LessonId lessonId = new LessonId(UUID.fromString("20000000-0000-0000-0000-000000000001"));
    private LessonVersionId versionId =
            new LessonVersionId(UUID.fromString("20000000-0000-0000-0000-000000000002"));
    private UUID teacherAccountId = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private LessonVersionReadiness readiness = LessonVersionReadiness.CLASSROOM_READY;

    public LessonVersionFixtureBuilder withIds(UUID lessonId, UUID versionId) {
        this.lessonId = new LessonId(lessonId);
        this.versionId = new LessonVersionId(versionId);
        return this;
    }

    public LessonVersionFixtureBuilder withTeacherAccountId(UUID teacherAccountId) {
        this.teacherAccountId = teacherAccountId;
        return this;
    }

    public LessonVersionFixtureBuilder asDraft() {
        this.readiness = LessonVersionReadiness.DRAFT;
        return this;
    }

    public Fixture build() {
        Lesson lesson = new Lesson(lessonId, teacherAccountId, Instant.parse("2026-01-01T10:03:00Z"));
        LessonSceneId firstSceneId =
                new LessonSceneId(UUID.fromString("20000000-0000-0000-0000-000000000003"));
        LessonSceneId secondSceneId =
                new LessonSceneId(UUID.fromString("20000000-0000-0000-0000-000000000004"));
        LessonScene firstScene = new LessonScene(
                firstSceneId,
                versionId,
                0,
                List.of(new SceneBlock(
                        new SceneBlockId(UUID.fromString("20000000-0000-0000-0000-000000000005")),
                        firstSceneId,
                        0,
                        SceneBlockType.PLAIN_TEXT,
                        "Deterministic first classroom scene")));
        LessonScene secondScene = new LessonScene(
                secondSceneId,
                versionId,
                1,
                List.of(new SceneBlock(
                        new SceneBlockId(UUID.fromString("20000000-0000-0000-0000-000000000006")),
                        secondSceneId,
                        0,
                        SceneBlockType.PLAIN_TEXT,
                        "Deterministic second classroom scene")));
        LessonVersion version = new LessonVersion(
                versionId,
                lessonId,
                readiness,
                Instant.parse("2026-01-01T10:04:00Z"),
                List.of(firstScene, secondScene));
        return new Fixture(lesson, version);
    }

    public record Fixture(Lesson lesson, LessonVersion version) {}
}
