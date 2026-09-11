package io.github.sipratama.penatika.classroom.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PairingGrantTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-09-11T02:00:00Z");

    @Test
    void issuanceLocksTheExactFiveMinuteLifetimeAndRedactsVerifierState() {
        PairingGrant grant = PairingGrant.issue(
                new PairingGrantId(UUID.randomUUID()),
                new ClassroomSessionId(UUID.randomUUID()),
                PairingRole.TEACHER_CONTROLLER,
                "a".repeat(64),
                ISSUED_AT);

        assertThat(grant.expiresAt()).isEqualTo(ISSUED_AT.plusSeconds(300));
        assertThat(grant.toString()).contains("REDACTED").doesNotContain(grant.credentialVerifier());
    }

    @Test
    void rejectsAnyLifetimeOtherThanExactlyFiveMinutesAndConsumptionAtExpiry() {
        assertThatThrownBy(() -> grant(ISSUED_AT.plusSeconds(301), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> grant(ISSUED_AT.plusSeconds(300), ISSUED_AT.plusSeconds(300)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static PairingGrant grant(Instant expiresAt, Instant consumedAt) {
        return new PairingGrant(
                new PairingGrantId(UUID.randomUUID()),
                new ClassroomSessionId(UUID.randomUUID()),
                PairingRole.CLASSROOM_DISPLAY,
                "b".repeat(64),
                ISSUED_AT,
                expiresAt,
                consumedAt,
                null);
    }
}
