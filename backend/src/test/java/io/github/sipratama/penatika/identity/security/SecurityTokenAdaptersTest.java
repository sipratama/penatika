package io.github.sipratama.penatika.identity.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.github.sipratama.penatika.identity.adapter.out.security.SecureRandomSecurityTokenGenerator;
import io.github.sipratama.penatika.identity.adapter.out.security.Sha256SecurityTokenVerifier;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

class SecurityTokenAdaptersTest {

    private final Sha256SecurityTokenVerifier verifier = new Sha256SecurityTokenVerifier();

    @Test
    void generatesExactlyThirtyTwoRandomBytesAsUnpaddedBase64Url() {
        SecureRandomSecurityTokenGenerator generator =
                new SecureRandomSecurityTokenGenerator(new SecureRandom());

        RawSecurityToken first = generator.generate();
        RawSecurityToken second = generator.generate();

        assertThat(first.expose())
                .hasSize(RawSecurityToken.ENCODED_LENGTH)
                .matches("[A-Za-z0-9_-]{43}")
                .doesNotContain("=");
        assertThat(Base64.getUrlDecoder().decode(first.expose()))
                .hasSize(SecureRandomSecurityTokenGenerator.RANDOM_BYTE_COUNT);
        assertThat(second.expose()).isNotEqualTo(first.expose());
    }

    @Test
    void hashesTokensAsLowercaseSha256AndComparesSafely() {
        RawSecurityToken token = RawSecurityToken.fromEncoded("A".repeat(43));
        String hash = verifier.verifierFor(token);

        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(verifier.matches(token, hash)).isTrue();
        assertThat(verifier.matches(RawSecurityToken.fromEncoded("B".repeat(43)), hash)).isFalse();
        assertThat(verifier.matches(token, null)).isFalse();
        assertThat(verifier.matches(token, "not-a-sha256-verifier")).isFalse();
    }

    @Test
    void rejectsMalformedTokensAndRedactsRawValues() {
        String raw = "S".repeat(43);
        RawSecurityToken token = RawSecurityToken.fromEncoded(raw);

        assertThat(token.toString()).doesNotContain(raw).contains("REDACTED");
        assertThatThrownBy(() -> RawSecurityToken.fromEncoded("short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotContaining("short");
        assertThatThrownBy(() -> RawSecurityToken.fromEncoded("+".repeat(43)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
