package io.github.sipratama.penatika.classroom.adapter.out.persistence.postgres;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.sipratama.penatika.classroom.application.port.out.AcceptedCommandOutcomePersistencePort;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcome;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcomeId;
import io.github.sipratama.penatika.classroom.domain.ClassroomAction;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.CommandType;
import io.github.sipratama.penatika.classroom.domain.Revision;

public final class PostgresAcceptedCommandOutcomePersistenceAdapter
        implements AcceptedCommandOutcomePersistencePort {

    private final JdbcClient jdbcClient;

    public PostgresAcceptedCommandOutcomePersistenceAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean saveIfAbsent(AcceptedCommandOutcome outcome) {
        return jdbcClient.sql("""
                        INSERT INTO classroom_accepted_command (
                            id, classroom_session_id, command_id, expected_revision,
                            command_type, action, teacher_account_id, teacher_browser_session_id,
                            controller_participant_session_id, controller_role,
                            resulting_revision, accepted_at)
                        VALUES (
                            :id, :classroomSessionId, :commandId, :expectedRevision,
                            :commandType, :action, :teacherAccountId, :teacherBrowserSessionId,
                            :controllerParticipantSessionId, 'TEACHER_CONTROLLER',
                            :resultingRevision, :acceptedAt)
                        ON CONFLICT (classroom_session_id, command_id) DO NOTHING
                        """)
                .param("id", outcome.id().value())
                .param("classroomSessionId", outcome.classroomSessionId().value())
                .param("commandId", outcome.commandId())
                .param("expectedRevision", outcome.expectedRevision().value())
                .param("commandType", outcome.commandType().name())
                .param("action", outcome.action().name())
                .param("teacherAccountId", outcome.teacherAccountId())
                .param("teacherBrowserSessionId", outcome.teacherBrowserSessionId())
                .param("controllerParticipantSessionId", outcome.controllerParticipantSessionId())
                .param("resultingRevision", outcome.resultingRevision().value())
                .param("acceptedAt", OffsetDateTime.ofInstant(outcome.acceptedAt(), java.time.ZoneOffset.UTC))
                .update() == 1;
    }

    @Override
    public Optional<AcceptedCommandOutcome> findByCommandIdentity(
            ClassroomSessionId classroomSessionId, String commandId) {
        return jdbcClient.sql("""
                        SELECT id, classroom_session_id, command_id, expected_revision,
                               command_type, action, teacher_account_id, teacher_browser_session_id,
                               controller_participant_session_id, resulting_revision, accepted_at
                        FROM classroom_accepted_command
                        WHERE classroom_session_id = :classroomSessionId AND command_id = :commandId
                        """)
                .param("classroomSessionId", classroomSessionId.value())
                .param("commandId", commandId)
                .query((row, rowNumber) -> new AcceptedCommandOutcome(
                        new AcceptedCommandOutcomeId(row.getObject("id", UUID.class)),
                        new ClassroomSessionId(row.getObject("classroom_session_id", UUID.class)),
                        row.getString("command_id"),
                        new Revision(row.getLong("expected_revision")),
                        CommandType.valueOf(row.getString("command_type")),
                        ClassroomAction.valueOf(row.getString("action")),
                        row.getObject("teacher_account_id", UUID.class),
                        row.getObject("teacher_browser_session_id", UUID.class),
                        row.getObject("controller_participant_session_id", UUID.class),
                        new Revision(row.getLong("resulting_revision")),
                        row.getObject("accepted_at", OffsetDateTime.class).toInstant()))
                .optional();
    }
}
