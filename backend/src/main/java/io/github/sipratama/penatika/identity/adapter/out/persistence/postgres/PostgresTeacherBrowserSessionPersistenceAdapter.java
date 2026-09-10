package io.github.sipratama.penatika.identity.adapter.out.persistence.postgres;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public final class PostgresTeacherBrowserSessionPersistenceAdapter
        implements TeacherBrowserSessionPersistencePort {

    private final JdbcClient jdbcClient;

    public PostgresTeacherBrowserSessionPersistenceAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void create(TeacherBrowserSession session) {
        jdbcClient.sql("""
                        INSERT INTO identity_teacher_browser_session (
                            id, teacher_account_id, credential_verifier, csrf_verifier,
                            created_at, last_active_at, idle_expires_at, absolute_expires_at, revoked_at)
                        VALUES (
                            :id, :teacherAccountId, :credentialVerifier, :csrfVerifier,
                            :createdAt, :lastActiveAt, :idleExpiresAt, :absoluteExpiresAt, :revokedAt)
                        """)
                .param("id", session.id().value())
                .param("teacherAccountId", session.teacherAccountId().value())
                .param("credentialVerifier", session.credentialVerifier())
                .param("csrfVerifier", session.csrfVerifier())
                .param("createdAt", timestamp(session.createdAt()))
                .param("lastActiveAt", timestamp(session.lastActiveAt()))
                .param("idleExpiresAt", timestamp(session.idleExpiresAt()))
                .param("absoluteExpiresAt", timestamp(session.absoluteExpiresAt()))
                .param("revokedAt", timestamp(session.revokedAt()), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .update();
    }

    @Override
    public Optional<TeacherBrowserSession> findByCredentialVerifier(String credentialVerifier) {
        return jdbcClient.sql("""
                        SELECT id, teacher_account_id, credential_verifier, csrf_verifier,
                               created_at, last_active_at, idle_expires_at, absolute_expires_at, revoked_at
                        FROM identity_teacher_browser_session
                        WHERE credential_verifier = :credentialVerifier
                        """)
                .param("credentialVerifier", credentialVerifier)
                .query((row, rowNumber) -> new TeacherBrowserSession(
                        new TeacherBrowserSessionId(row.getObject("id", UUID.class)),
                        new TeacherAccountId(row.getObject("teacher_account_id", UUID.class)),
                        row.getString("credential_verifier").trim(),
                        row.getString("csrf_verifier").trim(),
                        instant(row.getObject("created_at", OffsetDateTime.class)),
                        instant(row.getObject("last_active_at", OffsetDateTime.class)),
                        instant(row.getObject("idle_expires_at", OffsetDateTime.class)),
                        instant(row.getObject("absolute_expires_at", OffsetDateTime.class)),
                        instant(row.getObject("revoked_at", OffsetDateTime.class))))
                .optional();
    }

    @Override
    public boolean revoke(TeacherBrowserSessionId id, Instant revokedAt) {
        return jdbcClient.sql("""
                        UPDATE identity_teacher_browser_session
                        SET revoked_at = :revokedAt
                        WHERE id = :id AND revoked_at IS NULL
                        """)
                .param("id", id.value())
                .param("revokedAt", timestamp(revokedAt))
                .update() == 1;
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static OffsetDateTime timestamp(Instant value) {
        return value == null ? null : OffsetDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
