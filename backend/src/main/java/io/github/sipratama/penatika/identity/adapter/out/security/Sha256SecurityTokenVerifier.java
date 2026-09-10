package io.github.sipratama.penatika.identity.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenVerifierPort;

public final class Sha256SecurityTokenVerifier implements SecurityTokenVerifierPort {

    private static final HexFormat LOWERCASE_HEX = HexFormat.of();

    @Override
    public String verifierFor(RawSecurityToken token) {
        return LOWERCASE_HEX.formatHex(digest(token));
    }

    @Override
    public boolean matches(RawSecurityToken token, String verifier) {
        if (verifier == null || verifier.length() != 64) {
            return false;
        }
        try {
            return MessageDigest.isEqual(digest(token), LOWERCASE_HEX.parseHex(verifier));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] digest(RawSecurityToken token) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(token.expose().getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
