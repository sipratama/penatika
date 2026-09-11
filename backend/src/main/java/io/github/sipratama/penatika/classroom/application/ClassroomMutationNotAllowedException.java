package io.github.sipratama.penatika.classroom.application;

public final class ClassroomMutationNotAllowedException extends RuntimeException {

    public ClassroomMutationNotAllowedException() {
        super("The classroom session does not currently permit mutation");
    }
}
