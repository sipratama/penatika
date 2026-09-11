package io.github.sipratama.penatika.classroom.application;

public final class UnexpectedAcceptedCommandConflictException extends RuntimeException {

    public UnexpectedAcceptedCommandConflictException() {
        super("Accepted command identity was concurrently persisted");
    }
}
