package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.time.Instant;

public record CreatePairingGrantResponse(
        String pairingGrantId,
        String pairingToken,
        String participantRole,
        Instant expiresAt) {

    @Override
    public String toString() {
        return "CreatePairingGrantResponse["
                + "pairingGrantId=" + pairingGrantId
                + ", pairingToken=[REDACTED]"
                + ", participantRole=" + participantRole
                + ", expiresAt=" + expiresAt
                + ']';
    }
}
