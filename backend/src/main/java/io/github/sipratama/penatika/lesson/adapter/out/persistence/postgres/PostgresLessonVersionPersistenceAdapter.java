package io.github.sipratama.penatika.lesson.adapter.out.persistence.postgres;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;
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

public final class PostgresLessonVersionPersistenceAdapter implements LessonVersionPersistencePort {

    private final JdbcClient jdbcClient;
    private final TransactionTemplate transactionTemplate;

    public PostgresLessonVersionPersistenceAdapter(
            JdbcClient jdbcClient, TransactionTemplate transactionTemplate) {
        this.jdbcClient = jdbcClient;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void createImmutableVersion(Lesson lesson, LessonVersion version) {
        if (!version.lessonId().equals(lesson.id())) {
            throw new IllegalArgumentException("LessonVersion must belong to the supplied Lesson");
        }
        transactionTemplate.executeWithoutResult(status -> {
            jdbcClient.sql("""
                            INSERT INTO lesson_lesson (id, teacher_account_id, created_at)
                            VALUES (:id, :teacherAccountId, :createdAt)
                            """)
                    .param("id", lesson.id().value())
                    .param("teacherAccountId", lesson.teacherAccountId())
                    .param("createdAt", timestamp(lesson.createdAt()))
                    .update();
            jdbcClient.sql("""
                            INSERT INTO lesson_lesson_version (id, lesson_id, readiness_status, created_at)
                            VALUES (:id, :lessonId, :readinessStatus, :createdAt)
                            """)
                    .param("id", version.id().value())
                    .param("lessonId", version.lessonId().value())
                    .param("readinessStatus", version.readiness().name())
                    .param("createdAt", timestamp(version.createdAt()))
                    .update();
            for (LessonScene scene : version.scenes()) {
                jdbcClient.sql("""
                                INSERT INTO lesson_lesson_scene (id, lesson_version_id, position)
                                VALUES (:id, :lessonVersionId, :position)
                                """)
                        .param("id", scene.id().value())
                        .param("lessonVersionId", scene.lessonVersionId().value())
                        .param("position", scene.position())
                        .update();
                for (SceneBlock block : scene.blocks()) {
                    jdbcClient.sql("""
                                    INSERT INTO lesson_scene_block
                                        (id, lesson_scene_id, position, block_type, plain_text)
                                    VALUES (:id, :lessonSceneId, :position, :blockType, :plainText)
                                    """)
                            .param("id", block.id().value())
                            .param("lessonSceneId", block.lessonSceneId().value())
                            .param("position", block.position())
                            .param("blockType", block.type().name())
                            .param("plainText", block.plainText())
                            .update();
                }
            }
        });
    }

    @Override
    public Optional<LessonVersion> findById(LessonVersionId lessonVersionId) {
        return load("v.id = :lessonVersionId", lessonVersionId, null);
    }

    @Override
    public Optional<LessonVersion> findClassroomReadyForTeacher(
            LessonVersionId lessonVersionId, UUID teacherAccountId) {
        return load(
                "v.id = :lessonVersionId AND l.teacher_account_id = :teacherAccountId "
                        + "AND v.readiness_status = 'CLASSROOM_READY'",
                lessonVersionId,
                teacherAccountId);
    }

    private Optional<LessonVersion> load(
            String predicate, LessonVersionId lessonVersionId, UUID teacherAccountId) {
        JdbcClient.StatementSpec statement = jdbcClient.sql("""
                        SELECT v.id AS version_id, v.lesson_id, v.readiness_status, v.created_at,
                               s.id AS scene_id, s.position AS scene_position,
                               b.id AS block_id, b.position AS block_position,
                               b.block_type, b.plain_text
                        FROM lesson_lesson_version v
                        JOIN lesson_lesson l ON l.id = v.lesson_id
                        LEFT JOIN lesson_lesson_scene s ON s.lesson_version_id = v.id
                        LEFT JOIN lesson_scene_block b ON b.lesson_scene_id = s.id
                        WHERE %s
                        ORDER BY s.position, b.position
                        """.formatted(predicate))
                .param("lessonVersionId", lessonVersionId.value());
        if (teacherAccountId != null) {
            statement = statement.param("teacherAccountId", teacherAccountId);
        }
        List<VersionRow> rows = statement.query((row, rowNumber) -> new VersionRow(
                        row.getObject("version_id", UUID.class),
                        row.getObject("lesson_id", UUID.class),
                        row.getString("readiness_status"),
                        row.getObject("created_at", OffsetDateTime.class),
                        row.getObject("scene_id", UUID.class),
                        nullableLong(row, "scene_position"),
                        row.getObject("block_id", UUID.class),
                        nullableLong(row, "block_position"),
                        row.getString("block_type"),
                        row.getString("plain_text")))
                .list();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        VersionRow first = rows.getFirst();
        Map<UUID, SceneAccumulator> scenes = new LinkedHashMap<>();
        for (VersionRow row : rows) {
            if (row.sceneId() == null) {
                continue;
            }
            SceneAccumulator scene = scenes.computeIfAbsent(
                    row.sceneId(),
                    ignored -> new SceneAccumulator(row.sceneId(), row.scenePosition(), new ArrayList<>()));
            if (row.blockId() != null) {
                scene.blocks().add(new SceneBlock(
                        new SceneBlockId(row.blockId()),
                        new LessonSceneId(row.sceneId()),
                        row.blockPosition(),
                        SceneBlockType.valueOf(row.blockType()),
                        row.plainText()));
            }
        }
        List<LessonScene> mappedScenes = scenes.values().stream()
                .map(scene -> new LessonScene(
                        new LessonSceneId(scene.id()),
                        lessonVersionId,
                        scene.position(),
                        scene.blocks()))
                .toList();
        return Optional.of(new LessonVersion(
                lessonVersionId,
                new LessonId(first.lessonId()),
                LessonVersionReadiness.valueOf(first.readinessStatus()),
                first.createdAt().toInstant(),
                mappedScenes));
    }

    private static Long nullableLong(java.sql.ResultSet row, String column) throws java.sql.SQLException {
        long value = row.getLong(column);
        return row.wasNull() ? null : value;
    }

    private static OffsetDateTime timestamp(java.time.Instant instant) {
        return OffsetDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
    }

    private record VersionRow(
            UUID versionId,
            UUID lessonId,
            String readinessStatus,
            OffsetDateTime createdAt,
            UUID sceneId,
            Long scenePosition,
            UUID blockId,
            Long blockPosition,
            String blockType,
            String plainText) {}

    private record SceneAccumulator(UUID id, long position, List<SceneBlock> blocks) {}
}
