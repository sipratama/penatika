package io.github.sipratama.penatika.classroom.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;
import io.github.sipratama.penatika.classroom.application.model.RawPairingToken;
import io.github.sipratama.penatika.classroom.application.port.out.PairingTokenVerifierPort;

public final class Sha256PairingTokenVerifier implements PairingTokenVerifierPort {

    private static final HexFormat LOWERCASE_HEX = HexFormat.of();

    @Override
    public String verifierFor(RawPairingToken token) {
        return verifierFor(token.expose());
    }

    @Override
    public String verifierFor(PresentedPairingToken token) {
        return verifierFor(token.expose());
    }

    private static String verifierFor(String token) {
        try {
            return LOWERCASE_HEX.formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
