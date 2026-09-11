package io.github.sipratama.penatika.classroom.adapter.out.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import io.github.sipratama.penatika.classroom.application.model.RawPairingToken;
import io.github.sipratama.penatika.classroom.application.port.out.PairingTokenGeneratorPort;

public final class SecureRandomPairingTokenGenerator implements PairingTokenGeneratorPort {

    public static final int RANDOM_BYTE_COUNT = 32;

    private final SecureRandom secureRandom;

    public SecureRandomPairingTokenGenerator(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom must not be null");
    }

    @Override
    public RawPairingToken generate() {
        byte[] bytes = new byte[RANDOM_BYTE_COUNT];
        secureRandom.nextBytes(bytes);
        return RawPairingToken.fromGenerated(
                Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }
}
