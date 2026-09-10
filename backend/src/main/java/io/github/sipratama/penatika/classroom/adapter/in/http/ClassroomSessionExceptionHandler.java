package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.sipratama.penatika.lesson.application.LessonVersionNotFoundOrUnauthorizedException;
import io.github.sipratama.penatika.lesson.application.LessonVersionNotReadyException;

@RestControllerAdvice(assignableTypes = ClassroomSessionController.class)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class ClassroomSessionExceptionHandler {

    @ExceptionHandler(ClassroomRequestValidationException.class)
    ResponseEntity<RequestValidationProblem> requestValidation(
            ClassroomRequestValidationException exception) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new RequestValidationProblem(exception.fieldErrors()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<RequestValidationProblem> malformedJson() {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new RequestValidationProblem(List.of(new RequestValidationFieldError(
                        "request", "INVALID_VALUE", "The request body is not valid JSON."))));
    }

    @ExceptionHandler(LessonVersionNotFoundOrUnauthorizedException.class)
    ResponseEntity<ClassroomSessionProblem> lessonVersionNotFound() {
        return ResponseEntity.status(404)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new ClassroomSessionProblem(
                        "Not Found",
                        404,
                        "The requested lesson version was not found.",
                        "LESSON_VERSION_NOT_FOUND"));
    }

    @ExceptionHandler(LessonVersionNotReadyException.class)
    ResponseEntity<ClassroomSessionProblem> lessonVersionNotReady() {
        return ResponseEntity.status(409)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new ClassroomSessionProblem(
                        "Conflict",
                        409,
                        "The lesson version is not ready for classroom use.",
                        "LESSON_VERSION_NOT_READY"));
    }
}
