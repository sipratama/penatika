package io.github.sipratama.penatika.identity.application;

public final class ParticipantRoleOccupiedException extends RuntimeException {

    public ParticipantRoleOccupiedException() {
        super("The participant role is already occupied");
    }
}
