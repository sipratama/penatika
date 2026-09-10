package io.github.sipratama.penatika.identity.application;

public final class TeacherAuthenticationRejectedException extends RuntimeException {

    public TeacherAuthenticationRejectedException() {
        super("Teacher authentication could not establish a Penatika session");
    }
}
