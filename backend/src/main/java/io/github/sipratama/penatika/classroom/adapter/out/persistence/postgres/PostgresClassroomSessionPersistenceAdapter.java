package io.github.sipratama.penatika.classroom.adapter.out.persistence.postgres;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.Revision;

public final class PostgresClassroomSessionPersistenceAdapter
        implements ClassroomSessionPersistencePort {

    private final JdbcClient jdbcClient;

    public PostgresClassroomSessionPersistenceAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void create(ClassroomSession session) {
        jdbcClient.sql("""
                        INSERT INTO classroom_session (
                            id, teacher_account_id, lesson_version_id, lifecycle_state,
                            current_scene_position, revision, started_at)
                        VALUES (
                            :id, :teacherAccountId, :lessonVersionId, :lifecycleState,
                            :currentScenePosition, :revision, :startedAt)
                        """)
                .param("id", session.id().value())
                .param("teacherAccountId", session.teacherAccountId())
                .param("lessonVersionId", session.lessonVersionId())
                .param("lifecycleState", session.lifecycleState().name())
                .param("currentScenePosition", session.currentScenePosition())
                .param("revision", session.revision().value())
                .param("startedAt", OffsetDateTime.ofInstant(session.startedAt(), java.time.ZoneOffset.UTC))
                .update();
    }

    @Override
    public Optional<ClassroomSession> findById(ClassroomSessionId classroomSessionId) {
        return queryById(classroomSessionId, "");
    }

    @Override
    public Optional<ClassroomSession> lockById(ClassroomSessionId classroomSessionId) {
        return queryById(classroomSessionId, " FOR UPDATE");
    }

    private Optional<ClassroomSession> queryById(
            ClassroomSessionId classroomSessionId, String lockingClause) {
        return jdbcClient.sql("""
                        SELECT id, teacher_account_id, lesson_version_id, lifecycle_state,
                               current_scene_position, revision, started_at
                        FROM classroom_session
                        WHERE id = :id
                        """ + lockingClause)
                .param("id", classroomSessionId.value())
                .query((row, rowNumber) -> new ClassroomSession(
                        new ClassroomSessionId(row.getObject("id", UUID.class)),
                        row.getObject("teacher_account_id", UUID.class),
                        row.getObject("lesson_version_id", UUID.class),
                        ClassroomLifecycleState.valueOf(row.getString("lifecycle_state")),
                        row.getLong("current_scene_position"),
                        new Revision(row.getLong("revision")),
                        row.getObject("started_at", OffsetDateTime.class).toInstant()))
                .optional();
    }

    @Override
    public boolean updatePositionIfRevisionMatches(
            ClassroomSessionId classroomSessionId,
            Revision expectedRevision,
            long newPosition,
            Revision resultingRevision,
            ClassroomLifecycleState lifecycleState) {
        if (resultingRevision.value() <= expectedRevision.value()) {
            throw new IllegalArgumentException("resultingRevision must advance expectedRevision");
        }
        return jdbcClient.sql("""
                        UPDATE classroom_session
                        SET current_scene_position = :newPosition,
                            revision = :resultingRevision,
                            lifecycle_state = :lifecycleState
                        WHERE id = :id AND revision = :expectedRevision
                        """)
                .param("newPosition", newPosition)
                .param("resultingRevision", resultingRevision.value())
                .param("lifecycleState", lifecycleState.name())
                .param("id", classroomSessionId.value())
                .param("expectedRevision", expectedRevision.value())
                .update() == 1;
    }
}
