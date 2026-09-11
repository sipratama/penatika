package io.github.sipratama.penatika.classroom.application;

public final class ParticipantRoleAlreadyActiveException extends RuntimeException {

    public ParticipantRoleAlreadyActiveException() {
        super("The requested participant role is already active");
    }
}
