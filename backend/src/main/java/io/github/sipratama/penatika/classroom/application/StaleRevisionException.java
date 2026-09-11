package io.github.sipratama.penatika.classroom.application;

public final class StaleRevisionException extends RuntimeException {

    public StaleRevisionException() {
        super("The command revision does not match authoritative state");
    }
}
