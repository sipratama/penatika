package io.github.sipratama.penatika.classroom.domain;

public record Revision(long value) {

    public static final long MAX_VALUE = 9_007_199_254_740_991L;

    public Revision {
        if (value < 0 || value > MAX_VALUE) {
            throw new IllegalArgumentException("revision must be within the wire-safe integer range");
        }
    }
}
