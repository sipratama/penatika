package io.github.sipratama.penatika.classroom.application.model;

import java.time.Instant;
import java.util.Objects;

public record IssuedPairingGrant(
        String pairingGrantId,
        RawPairingToken pairingToken,
        String participantRole,
        Instant expiresAt) {

    public IssuedPairingGrant {
        Objects.requireNonNull(pairingGrantId, "pairingGrantId must not be null");
        Objects.requireNonNull(pairingToken, "pairingToken must not be null");
        Objects.requireNonNull(participantRole, "participantRole must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    @Override
    public String toString() {
        return "IssuedPairingGrant[REDACTED]";
    }
}
