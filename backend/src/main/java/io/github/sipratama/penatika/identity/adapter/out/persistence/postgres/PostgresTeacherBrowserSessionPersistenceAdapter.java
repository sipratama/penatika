package io.github.sipratama.penatika.identity.adapter.out.persistence.postgres;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;

public final class PostgresTeacherBrowserSessionPersistenceAdapter
        implements TeacherBrowserSessionPersistencePort {

    private final JdbcClient jdbcClient;
    private final TransactionTemplate transactionTemplate;

    public PostgresTeacherBrowserSessionPersistenceAdapter(
            JdbcClient jdbcClient, TransactionTemplate transactionTemplate) {
        this.jdbcClient = jdbcClient;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void createReplacing(
            TeacherBrowserSession session,
            Optional<String> replacedCredentialVerifier,
            Instant replacedAt) {
        transactionTemplate.executeWithoutResult(status -> {
            create(session);
            replacedCredentialVerifier.ifPresent(verifier -> jdbcClient.sql("""
                            UPDATE identity_teacher_browser_session
                            SET revoked_at = :revokedAt
                            WHERE credential_verifier = :credentialVerifier
                              AND revoked_at IS NULL
                              AND :revokedAt < idle_expires_at
                              AND :revokedAt < absolute_expires_at
                            """)
                    .param("revokedAt", timestamp(replacedAt))
                    .param("credentialVerifier", verifier)
                    .update());
        });
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
        return querySession("""
                        SELECT id, teacher_account_id, credential_verifier, csrf_verifier,
                               created_at, last_active_at, idle_expires_at, absolute_expires_at, revoked_at
                        FROM identity_teacher_browser_session
                        WHERE credential_verifier = :credentialVerifier
                        """, "credentialVerifier", credentialVerifier);
    }

    @Override
    public Optional<TeacherBrowserSession> findById(TeacherBrowserSessionId id) {
        return querySession("""
                        SELECT id, teacher_account_id, credential_verifier, csrf_verifier,
                               created_at, last_active_at, idle_expires_at, absolute_expires_at, revoked_at
                        FROM identity_teacher_browser_session
                        WHERE id = :id
                        """, "id", id.value());
    }

    @Override
    public boolean refreshActivity(
            TeacherBrowserSessionId id,
            Instant activityAt,
            Instant idleExpiresAt) {
        return jdbcClient.sql("""
                        UPDATE identity_teacher_browser_session AS session
                        SET last_active_at = :activityAt,
                            idle_expires_at = LEAST(:idleExpiresAt, session.absolute_expires_at)
                        WHERE session.id = :id
                          AND session.revoked_at IS NULL
                          AND :activityAt >= session.last_active_at
                          AND :activityAt < session.idle_expires_at
                          AND :activityAt < session.absolute_expires_at
                          AND EXISTS (
                              SELECT 1
                              FROM identity_teacher_account AS teacher
                              WHERE teacher.id = session.teacher_account_id
                                AND teacher.status = 'ACTIVE')
                        """)
                .param("id", id.value())
                .param("activityAt", timestamp(activityAt))
                .param("idleExpiresAt", timestamp(idleExpiresAt))
                .update() == 1;
    }

    @Override
    public boolean replaceCsrfVerifierAndRefreshActivity(
            TeacherBrowserSessionId id,
            String expectedCsrfVerifier,
            String replacementCsrfVerifier,
            Instant activityAt,
            Instant idleExpiresAt) {
        return jdbcClient.sql("""
                        UPDATE identity_teacher_browser_session AS session
                        SET csrf_verifier = :replacementCsrfVerifier,
                            last_active_at = :activityAt,
                            idle_expires_at = LEAST(:idleExpiresAt, session.absolute_expires_at)
                        WHERE session.id = :id
                          AND session.csrf_verifier = :expectedCsrfVerifier
                          AND session.revoked_at IS NULL
                          AND :activityAt >= session.last_active_at
                          AND :activityAt < session.idle_expires_at
                          AND :activityAt < session.absolute_expires_at
                          AND EXISTS (
                              SELECT 1
                              FROM identity_teacher_account AS teacher
                              WHERE teacher.id = session.teacher_account_id
                                AND teacher.status = 'ACTIVE')
                        """)
                .param("id", id.value())
                .param("expectedCsrfVerifier", expectedCsrfVerifier)
                .param("replacementCsrfVerifier", replacementCsrfVerifier)
                .param("activityAt", timestamp(activityAt))
                .param("idleExpiresAt", timestamp(idleExpiresAt))
                .update() == 1;
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

    private Optional<TeacherBrowserSession> querySession(String sql, String parameterName, Object parameterValue) {
        return jdbcClient.sql(sql)
                .param(parameterName, parameterValue)
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

    private static OffsetDateTime timestamp(Instant value) {
        return value == null ? null : OffsetDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
