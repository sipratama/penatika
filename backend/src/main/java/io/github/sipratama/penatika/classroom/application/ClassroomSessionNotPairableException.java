package io.github.sipratama.penatika.classroom.application;

public final class ClassroomSessionNotPairableException extends RuntimeException {

    public ClassroomSessionNotPairableException() {
        super("The Classroom Session does not permit pairing");
    }
}
