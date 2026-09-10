package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.github.sipratama.penatika.classroom.application.model.StartClassroomSessionCommand;
import io.github.sipratama.penatika.classroom.application.model.StartedClassroomSession;
import io.github.sipratama.penatika.classroom.application.port.in.StartClassroomSessionUseCase;
import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherMutation;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.in.AuthorizeTeacherMutationUseCase;
import io.github.sipratama.penatika.lesson.application.model.ClassroomStartLessonVersion;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveClassroomStartLessonVersionUseCase;
import tools.jackson.databind.JsonNode;

@RestController
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class ClassroomSessionController {

    private static final String CSRF_HEADER = "X-Penatika-CSRF";
    private static final Pattern NON_WHITESPACE = Pattern.compile("^\\S+$", Pattern.UNICODE_CHARACTER_CLASS);

    private final AuthorizeTeacherMutationUseCase authorizeTeacherMutation;
    private final ResolveClassroomStartLessonVersionUseCase resolveLessonVersion;
    private final StartClassroomSessionUseCase startClassroomSession;

    public ClassroomSessionController(
            AuthorizeTeacherMutationUseCase authorizeTeacherMutation,
            ResolveClassroomStartLessonVersionUseCase resolveLessonVersion,
            StartClassroomSessionUseCase startClassroomSession) {
        this.authorizeTeacherMutation = authorizeTeacherMutation;
        this.resolveLessonVersion = resolveLessonVersion;
        this.startClassroomSession = startClassroomSession;
    }

    @PostMapping(
            path = "/api/classroom-sessions",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<StartClassroomSessionResponse> startClassroomSession(
            @AuthenticationPrincipal(expression = "authenticatedSession")
                    AuthenticatedTeacherSession authenticatedSession,
            @RequestHeader(name = CSRF_HEADER, required = false) String csrfHeader,
            @RequestBody JsonNode requestBody) {
        String requestedLessonVersionId = validateRequest(requestBody);
        AuthorizedTeacherMutation authorizedTeacher = authorizeTeacherMutation.authorizeMutation(
                authenticatedSession, parseCsrfToken(csrfHeader));
        ClassroomStartLessonVersion lessonVersion = resolveLessonVersion.resolve(
                requestedLessonVersionId, authorizedTeacher.teacherAccountId());
        StartedClassroomSession startedSession = startClassroomSession.start(
                new StartClassroomSessionCommand(
                        authorizedTeacher.teacherAccountId(),
                        lessonVersion.internalId(),
                        lessonVersion.externalId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StartClassroomSessionResponse(startedSession));
    }

    private static String validateRequest(JsonNode requestBody) {
        if (!requestBody.isObject()) {
            throw invalidRequest("INVALID_VALUE", "The request body must be a JSON object.");
        }
        if (!requestBody.has("lessonVersionId")) {
            throw invalidLessonVersionId("REQUIRED", "The field is required.");
        }
        if (requestBody.size() != 1) {
            throw invalidRequest("ADDITIONAL_PROPERTY", "The request contains an unsupported property.");
        }

        JsonNode lessonVersionId = requestBody.get("lessonVersionId");
        if (lessonVersionId == null || !lessonVersionId.isString()) {
            throw invalidLessonVersionId(
                    "INVALID_VALUE", "The field value does not satisfy its contract.");
        }
        String value = lessonVersionId.stringValue();
        int length = value.codePointCount(0, value.length());
        if (length < 1 || length > 128 || !NON_WHITESPACE.matcher(value).matches()) {
            throw invalidLessonVersionId(
                    "INVALID_VALUE", "The field value does not satisfy its contract.");
        }
        return value;
    }

    private static Optional<RawSecurityToken> parseCsrfToken(String value) {
        try {
            return Optional.ofNullable(value).map(RawSecurityToken::fromEncoded);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static ClassroomRequestValidationException invalidLessonVersionId(
            String code, String message) {
        return new ClassroomRequestValidationException(List.of(
                new RequestValidationFieldError("lessonVersionId", code, message)));
    }

    private static ClassroomRequestValidationException invalidRequest(String code, String message) {
        return new ClassroomRequestValidationException(List.of(
                new RequestValidationFieldError("request", code, message)));
    }
}
