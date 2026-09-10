package io.github.sipratama.penatika.classroom.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record PairingGrant(
        PairingGrantId id,
        ClassroomSessionId classroomSessionId,
        PairingRole participantRole,
        String credentialVerifier,
        Instant issuedAt,
        Instant expiresAt,
        Instant consumedAt,
        Instant revokedAt) {

    public PairingGrant {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantRole, "participantRole must not be null");
        Objects.requireNonNull(credentialVerifier, "credentialVerifier must not be null");
        Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (!expiresAt.equals(issuedAt.plus(Duration.ofMinutes(5)))) {
            throw new IllegalArgumentException("PairingGrant must expire exactly five minutes after issuance");
        }
        if (consumedAt != null && revokedAt != null) {
            throw new IllegalArgumentException("PairingGrant cannot be consumed and revoked");
        }
    }

    public Optional<Instant> consumedAtOptional() {
        return Optional.ofNullable(consumedAt);
    }

    public Optional<Instant> revokedAtOptional() {
        return Optional.ofNullable(revokedAt);
    }
}
