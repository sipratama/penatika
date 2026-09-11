package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;
import java.util.regex.Pattern;

public final class RawPairingToken {

    public static final int ENCODED_LENGTH = 43;
    private static final Pattern BASE64_URL = Pattern.compile("^[A-Za-z0-9_-]{43}$");

    private final String value;

    private RawPairingToken(String value) {
        this.value = value;
    }

    public static RawPairingToken fromGenerated(String value) {
        Objects.requireNonNull(value, "pairing token must not be null");
        if (!BASE64_URL.matcher(value).matches()) {
            throw new IllegalArgumentException("generated pairing token has an invalid representation");
        }
        return new RawPairingToken(value);
    }

    public String expose() {
        return value;
    }

    @Override
    public String toString() {
        return "RawPairingToken[REDACTED]";
    }
}
