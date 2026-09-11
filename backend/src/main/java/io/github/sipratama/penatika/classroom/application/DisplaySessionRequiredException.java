package io.github.sipratama.penatika.classroom.application;

public final class DisplaySessionRequiredException extends RuntimeException {

    public DisplaySessionRequiredException() {
        super("A usable Display participant session is required");
    }
}
