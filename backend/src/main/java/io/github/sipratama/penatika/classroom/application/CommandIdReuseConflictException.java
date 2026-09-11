package io.github.sipratama.penatika.classroom.application;

public final class CommandIdReuseConflictException extends RuntimeException {

    public CommandIdReuseConflictException() {
        super("The command identity was reused for a different command");
    }
}
