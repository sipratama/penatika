package io.github.sipratama.penatika.classroom.adapter.in.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class CreatePairingGrantResponseTest {

    @Test
    void stringRepresentationRedactsTheRawPairingToken() {
        String rawPairingToken = "raw-one-time-pairing-token";
        CreatePairingGrantResponse response = new CreatePairingGrantResponse(
                "pairing-grant-id",
                rawPairingToken,
                "CLASSROOM_DISPLAY",
                Instant.parse("2026-09-11T08:05:00Z"));

        assertThat(response.toString())
                .contains("CreatePairingGrantResponse", "pairing-grant-id", "CLASSROOM_DISPLAY")
                .doesNotContain(rawPairingToken);
    }
}
