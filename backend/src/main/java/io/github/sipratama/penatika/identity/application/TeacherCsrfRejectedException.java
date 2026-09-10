package io.github.sipratama.penatika.identity.application;

public final class TeacherCsrfRejectedException extends RuntimeException {

    public TeacherCsrfRejectedException() {
        super("The Teacher request did not include valid CSRF protection");
    }
}
