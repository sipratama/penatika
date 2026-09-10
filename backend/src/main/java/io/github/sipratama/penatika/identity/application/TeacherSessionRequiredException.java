package io.github.sipratama.penatika.identity.application;

public final class TeacherSessionRequiredException extends RuntimeException {

    public TeacherSessionRequiredException() {
        super("An authenticated Teacher session is required");
    }
}
