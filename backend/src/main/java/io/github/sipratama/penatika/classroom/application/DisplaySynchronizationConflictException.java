package io.github.sipratama.penatika.classroom.application;

public final class DisplaySynchronizationConflictException extends RuntimeException {

    public DisplaySynchronizationConflictException() {
        super("The Display synchronization acknowledgement is not valid for current state");
    }
}
