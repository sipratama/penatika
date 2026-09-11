package io.github.sipratama.penatika.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ParticipantSessionTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-11T03:00:00Z");

    @Test
    void participantLifetimeIsFixedAtEightHoursAndExactExpiryIsUnusable() {
        ParticipantSession display = ParticipantSession.display(
                new ParticipantSessionId(UUID.randomUUID()),
                new ClassroomSessionReference(UUID.randomUUID()),
                "c".repeat(64),
                CREATED_AT);

        assertThat(display.expiresAt()).isEqualTo(CREATED_AT.plusSeconds(8 * 60 * 60));
        assertThat(display.isUsableAt(display.expiresAt().minusNanos(1))).isTrue();
        assertThat(display.isUsableAt(display.expiresAt())).isFalse();
        assertThat(display.toString()).contains("REDACTED").doesNotContain(display.credentialVerifier());
    }

    @Test
    void rejectsNullOrNonCanonicalExpiryAndEnforcesRoleSpecificTeacherContext() {
        assertThatThrownBy(() -> participant(ParticipantRole.CLASSROOM_DISPLAY, null, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> participant(
                        ParticipantRole.CLASSROOM_DISPLAY,
                        CREATED_AT.plusSeconds(8 * 60 * 60 + 1),
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> participant(
                        ParticipantRole.TEACHER_CONTROLLER,
                        CREATED_AT.plusSeconds(8 * 60 * 60),
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static ParticipantSession participant(
            ParticipantRole role,
            Instant expiresAt,
            TeacherAccountId teacherAccountId,
            TeacherBrowserSessionId browserSessionId) {
        return new ParticipantSession(
                new ParticipantSessionId(UUID.randomUUID()),
                new ClassroomSessionReference(UUID.randomUUID()),
                role,
                "d".repeat(64),
                teacherAccountId,
                browserSessionId,
                CREATED_AT,
                expiresAt,
                null);
    }
}
