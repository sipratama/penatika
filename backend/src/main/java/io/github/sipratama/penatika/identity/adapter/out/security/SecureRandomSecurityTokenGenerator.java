package io.github.sipratama.penatika.identity.adapter.out.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenGeneratorPort;

public final class SecureRandomSecurityTokenGenerator implements SecurityTokenGeneratorPort {

    public static final int RANDOM_BYTE_COUNT = 32;

    private final SecureRandom secureRandom;

    public SecureRandomSecurityTokenGenerator(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom must not be null");
    }

    @Override
    public RawSecurityToken generate() {
        byte[] bytes = new byte[RANDOM_BYTE_COUNT];
        secureRandom.nextBytes(bytes);
        return RawSecurityToken.fromEncoded(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }
}
