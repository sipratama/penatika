package io.github.sipratama.penatika.identity.application.model;

import java.util.Objects;
import java.util.regex.Pattern;

public final class RawSecurityToken {

    public static final int ENCODED_LENGTH = 43;
    private static final Pattern BASE64_URL = Pattern.compile("^[A-Za-z0-9_-]{43}$");

    private final String value;

    private RawSecurityToken(String value) {
        this.value = value;
    }

    public static RawSecurityToken fromEncoded(String value) {
        Objects.requireNonNull(value, "security token must not be null");
        if (!BASE64_URL.matcher(value).matches()) {
            throw new IllegalArgumentException("security token has an invalid representation");
        }
        return new RawSecurityToken(value);
    }

    public String expose() {
        return value;
    }

    @Override
    public String toString() {
        return "RawSecurityToken[REDACTED]";
    }
}
