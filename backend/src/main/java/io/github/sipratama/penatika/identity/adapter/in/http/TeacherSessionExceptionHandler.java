package io.github.sipratama.penatika.identity.adapter.in.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionCookies;
import io.github.sipratama.penatika.identity.application.TeacherSessionRequiredException;
import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice(assignableTypes = TeacherSessionController.class)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class TeacherSessionExceptionHandler {

    private final TeacherSessionCookies cookies;

    public TeacherSessionExceptionHandler(TeacherSessionCookies cookies) {
        this.cookies = cookies;
    }

    @ExceptionHandler(TeacherSessionRequiredException.class)
    ResponseEntity<TeacherSessionProblem> teacherSessionRequired(HttpServletResponse response) {
        cookies.clear(response);
        return ResponseEntity.status(401)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new TeacherSessionProblem());
    }
}
