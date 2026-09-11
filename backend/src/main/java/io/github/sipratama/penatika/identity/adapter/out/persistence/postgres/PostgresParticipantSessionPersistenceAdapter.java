package io.github.sipratama.penatika.identity.adapter.out.persistence.postgres;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.sipratama.penatika.identity.application.port.out.ParticipantSessionPersistencePort;
import io.github.sipratama.penatika.identity.domain.ClassroomSessionReference;
import io.github.sipratama.penatika.identity.domain.ParticipantRole;
import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.ParticipantSessionId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public final class PostgresParticipantSessionPersistenceAdapter
        implements ParticipantSessionPersistencePort {

    private final JdbcClient jdbcClient;

    public PostgresParticipantSessionPersistenceAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean tryCreateActive(ParticipantSession session) {
        return jdbcClient.sql("""
                        INSERT INTO identity_participant_session (
                            id, classroom_session_id, participant_role, credential_verifier,
                            teacher_account_id, teacher_browser_session_id, created_at, expires_at, revoked_at)
                        VALUES (
                            :id, :classroomSessionId, :participantRole, :credentialVerifier,
                            :teacherAccountId, :teacherBrowserSessionId, :createdAt, :expiresAt, :revokedAt)
                        ON CONFLICT (classroom_session_id, participant_role)
                            WHERE revoked_at IS NULL
                            DO NOTHING
                        """)
                .param("id", session.id().value())
                .param("classroomSessionId", session.classroomSessionId().value())
                .param("participantRole", session.participantRole().name())
                .param("credentialVerifier", session.credentialVerifier())
                .param("teacherAccountId", value(session.teacherAccountId()), java.sql.Types.OTHER)
                .param("teacherBrowserSessionId", value(session.teacherBrowserSessionId()), java.sql.Types.OTHER)
                .param("createdAt", timestamp(session.createdAt()))
                .param("expiresAt", timestamp(session.expiresAt()), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .param("revokedAt", timestamp(session.revokedAt()), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .update() == 1;
    }

    @Override
    public Optional<ParticipantSession> findByCredentialVerifier(String credentialVerifier) {
        return jdbcClient.sql("""
                        SELECT id, classroom_session_id, participant_role, credential_verifier,
                               teacher_account_id, teacher_browser_session_id, created_at, expires_at, revoked_at
                        FROM identity_participant_session
                        WHERE credential_verifier = :credentialVerifier
                          AND expires_at IS NOT NULL
                        """)
                .param("credentialVerifier", credentialVerifier)
                .query((row, rowNumber) -> map(row))
                .optional();
    }

    @Override
    public Optional<ParticipantSession> findActiveByRole(
            ClassroomSessionReference classroomSessionId,
            ParticipantRole participantRole) {
        return jdbcClient.sql("""
                        SELECT id, classroom_session_id, participant_role, credential_verifier,
                               teacher_account_id, teacher_browser_session_id, created_at, expires_at, revoked_at
                        FROM identity_participant_session
                        WHERE classroom_session_id = :classroomSessionId
                          AND participant_role = :participantRole
                          AND revoked_at IS NULL
                          AND expires_at IS NOT NULL
                        """)
                .param("classroomSessionId", classroomSessionId.value())
                .param("participantRole", participantRole.name())
                .query((row, rowNumber) -> map(row))
                .optional();
    }

    @Override
    public int revokeInvalidLifetimeOccupants(
            ClassroomSessionReference classroomSessionId,
            ParticipantRole participantRole,
            Instant now) {
        return jdbcClient.sql("""
                        UPDATE identity_participant_session
                        SET revoked_at = :revokedAt
                        WHERE classroom_session_id = :classroomSessionId
                          AND participant_role = :participantRole
                          AND revoked_at IS NULL
                          AND (expires_at IS NULL OR expires_at <= :now)
                        """)
                .param("revokedAt", timestamp(now))
                .param("classroomSessionId", classroomSessionId.value())
                .param("participantRole", participantRole.name())
                .param("now", timestamp(now))
                .update();
    }

    @Override
    public boolean revoke(ParticipantSessionId id, Instant revokedAt) {
        return jdbcClient.sql("""
                        UPDATE identity_participant_session
                        SET revoked_at = :revokedAt
                        WHERE id = :id AND revoked_at IS NULL
                        """)
                .param("id", id.value())
                .param("revokedAt", timestamp(revokedAt))
                .update() == 1;
    }

    private static UUID value(TeacherAccountId id) {
        return id == null ? null : id.value();
    }

    private static UUID value(TeacherBrowserSessionId id) {
        return id == null ? null : id.value();
    }

    private static TeacherAccountId teacherId(UUID value) {
        return value == null ? null : new TeacherAccountId(value);
    }

    private static TeacherBrowserSessionId browserSessionId(UUID value) {
        return value == null ? null : new TeacherBrowserSessionId(value);
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static OffsetDateTime timestamp(Instant value) {
        return value == null ? null : OffsetDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }

    private static ParticipantSession map(java.sql.ResultSet row) throws java.sql.SQLException {
        return new ParticipantSession(
                new ParticipantSessionId(row.getObject("id", UUID.class)),
                new ClassroomSessionReference(row.getObject("classroom_session_id", UUID.class)),
                ParticipantRole.valueOf(row.getString("participant_role")),
                row.getString("credential_verifier").trim(),
                teacherId(row.getObject("teacher_account_id", UUID.class)),
                browserSessionId(row.getObject("teacher_browser_session_id", UUID.class)),
                instant(row.getObject("created_at", OffsetDateTime.class)),
                instant(row.getObject("expires_at", OffsetDateTime.class)),
                instant(row.getObject("revoked_at", OffsetDateTime.class)));
    }
}
