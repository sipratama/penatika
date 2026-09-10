package io.github.sipratama.penatika.lesson.application;

public final class LessonVersionNotReadyException extends RuntimeException {

    public LessonVersionNotReadyException() {
        super("The lesson version is not ready for classroom use");
    }
}
