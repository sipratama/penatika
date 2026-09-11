package io.github.sipratama.penatika.classroom.application;

public final class DisplayAuthorityRequiredException extends RuntimeException {

    public DisplayAuthorityRequiredException() {
        super("Classroom Display authority is required");
    }
}
