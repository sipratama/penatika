package io.github.sipratama.penatika.classroom.application;

public final class DisplayIntentRequiredException extends RuntimeException {

    public DisplayIntentRequiredException() {
        super("A valid Display request-intent guard is required");
    }
}
