package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;
import java.util.regex.Pattern;

public final class PresentedPairingToken {

    private static final int MAX_CODE_POINTS = 512;
    private static final Pattern NON_WHITESPACE = Pattern.compile("^\\S+$", Pattern.UNICODE_CHARACTER_CLASS);

    private final String value;

    private PresentedPairingToken(String value) {
        this.value = value;
    }

    public static PresentedPairingToken fromWire(String value) {
        Objects.requireNonNull(value, "presented pairing token must not be null");
        int length = value.codePointCount(0, value.length());
        if (length < 1 || length > MAX_CODE_POINTS || !NON_WHITESPACE.matcher(value).matches()) {
            throw new IllegalArgumentException("presented pairing token has an invalid wire representation");
        }
        return new PresentedPairingToken(value);
    }

    public String expose() {
        return value;
    }

    @Override
    public String toString() {
        return "PresentedPairingToken[REDACTED]";
    }
}
