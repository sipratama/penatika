package io.github.sipratama.penatika.classroom.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.github.sipratama.penatika.classroom.adapter.out.security.SecureRandomPairingTokenGenerator;
import io.github.sipratama.penatika.classroom.adapter.out.security.Sha256PairingTokenVerifier;
import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;
import io.github.sipratama.penatika.classroom.application.model.RawPairingToken;

class PairingTokenAdaptersTest {

    private final Sha256PairingTokenVerifier verifier = new Sha256PairingTokenVerifier();

    @Test
    void generatesThirtyTwoRandomBytesAsFortyThreeCharacterUnpaddedBase64Url() {
        SecureRandomPairingTokenGenerator generator =
                new SecureRandomPairingTokenGenerator(new SecureRandom());

        RawPairingToken first = generator.generate();
        RawPairingToken second = generator.generate();

        assertThat(first.expose()).hasSize(43).matches("[A-Za-z0-9_-]{43}").doesNotContain("=");
        assertThat(Base64.getUrlDecoder().decode(first.expose())).hasSize(32);
        assertThat(second.expose()).isNotEqualTo(first.expose());
    }

    @Test
    void hashesGeneratedAndBroaderPresentedTokensWithoutExposingTheirValues() {
        String generatedValue = "A".repeat(43);
        String broaderWireValue = "B".repeat(50);
        RawPairingToken generated = RawPairingToken.fromGenerated(generatedValue);
        PresentedPairingToken presented = PresentedPairingToken.fromWire(broaderWireValue);

        assertThat(verifier.verifierFor(generated)).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(verifier.verifierFor(presented)).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(generated.toString()).contains("REDACTED").doesNotContain(generatedValue);
        assertThat(presented.toString()).contains("REDACTED").doesNotContain(broaderWireValue);
    }

    @Test
    void presentedWireTokenUsesCodePointsAndRejectsWhitespaceOrOutOfBoundsValues() {
        String supplementary = new String(Character.toChars(0x1F642));

        assertThat(PresentedPairingToken.fromWire(supplementary.repeat(512)).expose())
                .isEqualTo(supplementary.repeat(512));
        assertThatThrownBy(() -> PresentedPairingToken.fromWire(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PresentedPairingToken.fromWire("two words"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PresentedPairingToken.fromWire(supplementary.repeat(513)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
