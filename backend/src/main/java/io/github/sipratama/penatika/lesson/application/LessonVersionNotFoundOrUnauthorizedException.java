package io.github.sipratama.penatika.lesson.application;

public final class LessonVersionNotFoundOrUnauthorizedException extends RuntimeException {

    public LessonVersionNotFoundOrUnauthorizedException() {
        super("The requested lesson version was not found");
    }
}
