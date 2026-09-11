package io.github.sipratama.penatika.identity.application;

public final class ParticipantTeacherAuthorityInvalidException extends RuntimeException {

    public ParticipantTeacherAuthorityInvalidException() {
        super("Controller participant requires current Teacher authority");
    }
}
