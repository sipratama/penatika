package io.github.sipratama.penatika.classroom.application;

public final class ControllerAuthorityRequiredException extends RuntimeException {

    public ControllerAuthorityRequiredException() {
        super("Active Controller authority is required");
    }
}
