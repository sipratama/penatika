package io.github.sipratama.penatika.identity.adapter.out.persistence.postgres;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLink;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLinkId;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountStatus;

public final class PostgresTeacherIdentityPersistenceAdapter implements TeacherIdentityPersistencePort {

    private final JdbcClient jdbcClient;
    private final TransactionTemplate transactionTemplate;

    public PostgresTeacherIdentityPersistenceAdapter(
            JdbcClient jdbcClient, TransactionTemplate transactionTemplate) {
        this.jdbcClient = jdbcClient;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void create(TeacherAccount teacherAccount, ExternalIdentityLink externalIdentityLink) {
        if (!externalIdentityLink.teacherAccountId().equals(teacherAccount.id())) {
            throw new IllegalArgumentException("external identity must belong to the TeacherAccount");
        }
        transactionTemplate.executeWithoutResult(status -> {
            jdbcClient.sql("""
                            INSERT INTO identity_teacher_account (id, status, created_at)
                            VALUES (:id, :status, :createdAt)
                            """)
                    .param("id", teacherAccount.id().value())
                    .param("status", teacherAccount.status().name())
                    .param("createdAt", timestamp(teacherAccount.createdAt()))
                    .update();
            jdbcClient.sql("""
                            INSERT INTO identity_external_identity_link
                                (id, teacher_account_id, issuer, subject, created_at)
                            VALUES (:id, :teacherAccountId, :issuer, :subject, :createdAt)
                            """)
                    .param("id", externalIdentityLink.id().value())
                    .param("teacherAccountId", externalIdentityLink.teacherAccountId().value())
                    .param("issuer", externalIdentityLink.issuer())
                    .param("subject", externalIdentityLink.subject())
                    .param("createdAt", timestamp(externalIdentityLink.createdAt()))
                    .update();
        });
    }

    @Override
    public Optional<ExternalIdentityLink> findExternalIdentity(String issuer, String subject) {
        return jdbcClient.sql("""
                        SELECT id, teacher_account_id, issuer, subject, created_at
                        FROM identity_external_identity_link
                        WHERE issuer = :issuer AND subject = :subject
                        """)
                .param("issuer", issuer)
                .param("subject", subject)
                .query((row, rowNumber) -> new ExternalIdentityLink(
                        new ExternalIdentityLinkId(row.getObject("id", java.util.UUID.class)),
                        new TeacherAccountId(row.getObject("teacher_account_id", java.util.UUID.class)),
                        row.getString("issuer"),
                        row.getString("subject"),
                        row.getObject("created_at", java.time.OffsetDateTime.class).toInstant()))
                .optional();
    }

    @Override
    public Optional<TeacherAccount> findTeacherAccount(TeacherAccountId teacherAccountId) {
        return jdbcClient.sql("""
                        SELECT id, status, created_at
                        FROM identity_teacher_account
                        WHERE id = :id
                        """)
                .param("id", teacherAccountId.value())
                .query((row, rowNumber) -> new TeacherAccount(
                        new TeacherAccountId(row.getObject("id", java.util.UUID.class)),
                        TeacherAccountStatus.valueOf(row.getString("status")),
                        row.getObject("created_at", java.time.OffsetDateTime.class).toInstant()))
                .optional();
    }

    private static java.time.OffsetDateTime timestamp(java.time.Instant instant) {
        return java.time.OffsetDateTime.ofInstant(instant, java.time.ZoneOffset.UTC);
    }
}
