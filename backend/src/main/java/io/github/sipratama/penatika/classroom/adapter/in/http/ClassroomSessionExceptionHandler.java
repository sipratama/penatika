package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.sipratama.penatika.classroom.application.ClassroomSessionNotFoundException;
import io.github.sipratama.penatika.classroom.application.ClassroomSessionNotPairableException;
import io.github.sipratama.penatika.classroom.application.ClassroomMutationNotAllowedException;
import io.github.sipratama.penatika.classroom.application.CommandIdReuseConflictException;
import io.github.sipratama.penatika.classroom.application.ControllerAuthorityRequiredException;
import io.github.sipratama.penatika.classroom.application.PairingGrantRejectedException;
import io.github.sipratama.penatika.classroom.application.ParticipantRoleAlreadyActiveException;
import io.github.sipratama.penatika.classroom.application.StaleRevisionException;
import io.github.sipratama.penatika.lesson.application.LessonVersionNotFoundOrUnauthorizedException;
import io.github.sipratama.penatika.lesson.application.LessonVersionNotReadyException;

@RestControllerAdvice(assignableTypes = {
        ClassroomSessionController.class,
        ClassroomControllerCommandController.class,
        PairingGrantController.class,
        ParticipantEstablishmentController.class
})
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

    @ExceptionHandler(ClassroomSessionNotFoundException.class)
    ResponseEntity<ClassroomSessionProblem> classroomSessionNotFound() {
        return problem(
                404,
                "Not Found",
                "The requested classroom session was not found.",
                "CLASSROOM_SESSION_NOT_FOUND");
    }

    @ExceptionHandler(ClassroomSessionNotPairableException.class)
    ResponseEntity<ClassroomSessionProblem> classroomSessionNotPairable() {
        return problem(
                409,
                "Conflict",
                "The classroom session does not currently permit pairing.",
                "CLASSROOM_SESSION_NOT_PAIRABLE");
    }

    @ExceptionHandler(PairingGrantRejectedException.class)
    ResponseEntity<ClassroomSessionProblem> pairingGrantRejected() {
        return problem(
                403,
                "Forbidden",
                "The pairing grant cannot authorize this request.",
                "PAIRING_GRANT_REJECTED");
    }

    @ExceptionHandler(ParticipantRoleAlreadyActiveException.class)
    ResponseEntity<ClassroomSessionProblem> participantRoleAlreadyActive() {
        return problem(
                409,
                "Conflict",
                "An active participant already occupies the requested role.",
                "PARTICIPANT_ROLE_ALREADY_ACTIVE");
    }

    @ExceptionHandler(ControllerAuthorityRequiredException.class)
    ResponseEntity<ClassroomSessionProblem> controllerAuthorityRequired() {
        return problem(
                403,
                "Forbidden",
                "Active Controller authority is required for this request.",
                "CONTROLLER_AUTHORITY_REQUIRED");
    }

    @ExceptionHandler(StaleRevisionException.class)
    ResponseEntity<ClassroomSessionProblem> staleRevision() {
        return problem(
                409,
                "Conflict",
                "The command revision does not match authoritative state.",
                "STALE_REVISION");
    }

    @ExceptionHandler(CommandIdReuseConflictException.class)
    ResponseEntity<ClassroomSessionProblem> commandIdReuseConflict() {
        return problem(
                409,
                "Conflict",
                "The command identity was reused for a different command.",
                "COMMAND_ID_REUSE_CONFLICT");
    }

    @ExceptionHandler(ClassroomMutationNotAllowedException.class)
    ResponseEntity<ClassroomSessionProblem> classroomMutationNotAllowed() {
        return problem(
                409,
                "Conflict",
                "The classroom session does not currently permit mutation.",
                "CLASSROOM_MUTATION_NOT_ALLOWED");
    }

    private static ResponseEntity<ClassroomSessionProblem> problem(
            int status, String title, String detail, String code) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new ClassroomSessionProblem(title, status, detail, code));
    }
}
