package io.github.sipratama.penatika.classroom.application;

public final class ClassroomSessionNotFoundException extends RuntimeException {

    public ClassroomSessionNotFoundException() {
        super("The Classroom Session was not found");
    }
}
