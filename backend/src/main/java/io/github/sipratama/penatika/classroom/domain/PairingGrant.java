package io.github.sipratama.penatika.classroom.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public record PairingGrant(
        PairingGrantId id,
        ClassroomSessionId classroomSessionId,
        PairingRole participantRole,
        String credentialVerifier,
        Instant issuedAt,
        Instant expiresAt,
        Instant consumedAt,
        Instant revokedAt) {

    public static final Duration LIFETIME = Duration.ofMinutes(5);
    private static final Pattern SHA_256_VERIFIER = Pattern.compile("^[0-9a-f]{64}$");

    public PairingGrant {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
        Objects.requireNonNull(participantRole, "participantRole must not be null");
        Objects.requireNonNull(credentialVerifier, "credentialVerifier must not be null");
        Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (!SHA_256_VERIFIER.matcher(credentialVerifier).matches()) {
            throw new IllegalArgumentException("credentialVerifier must be lowercase SHA-256 hex");
        }
        if (!expiresAt.equals(issuedAt.plus(LIFETIME))) {
            throw new IllegalArgumentException("PairingGrant must expire exactly five minutes after issuance");
        }
        if (consumedAt != null && revokedAt != null) {
            throw new IllegalArgumentException("PairingGrant cannot be consumed and revoked");
        }
        if (consumedAt != null && (consumedAt.isBefore(issuedAt) || !consumedAt.isBefore(expiresAt))) {
            throw new IllegalArgumentException("consumedAt must be within the grant lifetime");
        }
        if (revokedAt != null && revokedAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("revokedAt must not precede issuedAt");
        }
    }

    public static PairingGrant issue(
            PairingGrantId id,
            ClassroomSessionId classroomSessionId,
            PairingRole participantRole,
            String credentialVerifier,
            Instant issuedAt) {
        return new PairingGrant(
                id,
                classroomSessionId,
                participantRole,
                credentialVerifier,
                issuedAt,
                issuedAt.plus(LIFETIME),
                null,
                null);
    }

    public Optional<Instant> consumedAtOptional() {
        return Optional.ofNullable(consumedAt);
    }

    public Optional<Instant> revokedAtOptional() {
        return Optional.ofNullable(revokedAt);
    }

    @Override
    public String toString() {
        return "PairingGrant[REDACTED]";
    }
}
