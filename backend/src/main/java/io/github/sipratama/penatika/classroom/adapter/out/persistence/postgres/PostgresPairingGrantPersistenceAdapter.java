package io.github.sipratama.penatika.classroom.adapter.out.persistence.postgres;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.sipratama.penatika.classroom.application.port.out.PairingGrantPersistencePort;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.PairingGrant;
import io.github.sipratama.penatika.classroom.domain.PairingGrantId;
import io.github.sipratama.penatika.classroom.domain.PairingRole;

public final class PostgresPairingGrantPersistenceAdapter implements PairingGrantPersistencePort {

    private final JdbcClient jdbcClient;

    public PostgresPairingGrantPersistenceAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void create(PairingGrant grant) {
        jdbcClient.sql("""
                        INSERT INTO classroom_pairing_grant (
                            id, classroom_session_id, participant_role, credential_verifier,
                            issued_at, expires_at, consumed_at, revoked_at)
                        VALUES (
                            :id, :classroomSessionId, :participantRole, :credentialVerifier,
                            :issuedAt, :expiresAt, :consumedAt, :revokedAt)
                        """)
                .param("id", grant.id().value())
                .param("classroomSessionId", grant.classroomSessionId().value())
                .param("participantRole", grant.participantRole().name())
                .param("credentialVerifier", grant.credentialVerifier())
                .param("issuedAt", timestamp(grant.issuedAt()))
                .param("expiresAt", timestamp(grant.expiresAt()))
                .param("consumedAt", timestamp(grant.consumedAt()), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .param("revokedAt", timestamp(grant.revokedAt()), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .update();
    }

    @Override
    public Optional<PairingGrant> findByCredentialVerifier(String credentialVerifier) {
        return jdbcClient.sql("""
                        SELECT id, classroom_session_id, participant_role, credential_verifier,
                               issued_at, expires_at, consumed_at, revoked_at
                        FROM classroom_pairing_grant
                        WHERE credential_verifier = :credentialVerifier
                        """)
                .param("credentialVerifier", credentialVerifier)
                .query(PostgresPairingGrantPersistenceAdapter::map)
                .optional();
    }

    @Override
    public Optional<PairingGrant> consumeByCredentialVerifier(
            String credentialVerifier, Instant consumedAt) {
        return jdbcClient.sql("""
                        UPDATE classroom_pairing_grant
                        SET consumed_at = :consumedAt
                        WHERE credential_verifier = :credentialVerifier
                          AND consumed_at IS NULL
                          AND revoked_at IS NULL
                          AND issued_at <= :consumedAt
                          AND expires_at > :consumedAt
                        RETURNING id, classroom_session_id, participant_role, credential_verifier,
                                  issued_at, expires_at, consumed_at, revoked_at
                        """)
                .param("credentialVerifier", credentialVerifier)
                .param("consumedAt", timestamp(consumedAt))
                .query(PostgresPairingGrantPersistenceAdapter::map)
                .optional();
    }

    @Override
    public boolean revoke(PairingGrantId pairingGrantId, Instant revokedAt) {
        return jdbcClient.sql("""
                        UPDATE classroom_pairing_grant
                        SET revoked_at = :revokedAt
                        WHERE id = :id AND consumed_at IS NULL AND revoked_at IS NULL
                        """)
                .param("id", pairingGrantId.value())
                .param("revokedAt", timestamp(revokedAt))
                .update() == 1;
    }

    private static PairingGrant map(java.sql.ResultSet row, int rowNumber) throws java.sql.SQLException {
        return new PairingGrant(
                new PairingGrantId(row.getObject("id", UUID.class)),
                new ClassroomSessionId(row.getObject("classroom_session_id", UUID.class)),
                PairingRole.valueOf(row.getString("participant_role")),
                row.getString("credential_verifier").trim(),
                row.getObject("issued_at", OffsetDateTime.class).toInstant(),
                row.getObject("expires_at", OffsetDateTime.class).toInstant(),
                instant(row.getObject("consumed_at", OffsetDateTime.class)),
                instant(row.getObject("revoked_at", OffsetDateTime.class)));
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static OffsetDateTime timestamp(Instant value) {
        return value == null ? null : OffsetDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
