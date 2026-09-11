package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.github.sipratama.penatika.classroom.application.ClassroomSessionNotFoundException;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandRequest;
import io.github.sipratama.penatika.classroom.application.port.in.ExecuteClassroomCommandUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.ReconcileControllerStateUseCase;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherMutation;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.in.AuthorizeTeacherMutationUseCase;
import io.github.sipratama.penatika.identity.application.port.in.AuthorizeTeacherReadUseCase;
import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.JsonNode;

@RestController
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class ClassroomControllerCommandController {

    private static final String CSRF_HEADER = "X-Penatika-CSRF";
    private static final int MAX_ID_CODE_POINTS = 128;
    private static final BigInteger MAX_REVISION = BigInteger.valueOf(Revision.MAX_VALUE);

    private final AuthorizeTeacherReadUseCase authorizeTeacherRead;
    private final AuthorizeTeacherMutationUseCase authorizeTeacherMutation;
    private final ReconcileControllerStateUseCase reconcileControllerState;
    private final ExecuteClassroomCommandUseCase executeCommand;
    private final ParticipantSessionCookies participantCookies;

    public ClassroomControllerCommandController(
            AuthorizeTeacherReadUseCase authorizeTeacherRead,
            AuthorizeTeacherMutationUseCase authorizeTeacherMutation,
            ReconcileControllerStateUseCase reconcileControllerState,
            ExecuteClassroomCommandUseCase executeCommand,
            ParticipantSessionCookies participantCookies) {
        this.authorizeTeacherRead = authorizeTeacherRead;
        this.authorizeTeacherMutation = authorizeTeacherMutation;
        this.reconcileControllerState = reconcileControllerState;
        this.executeCommand = executeCommand;
        this.participantCookies = participantCookies;
    }

    @GetMapping(
            path = "/api/classroom-sessions/{classroomSessionId}/controller-state",
            produces = "application/json")
    public ResponseEntity<ControllerReconciliationResponse> reconcile(
            @PathVariable String classroomSessionId,
            @AuthenticationPrincipal(expression = "authenticatedSession")
                    AuthenticatedTeacherSession authenticatedSession,
            HttpServletRequest request) {
        UUID internalId = parseClassroomSessionId(classroomSessionId);
        AuthorizedTeacherSession teacher = authorizeTeacherRead.authorizeRead(authenticatedSession);
        var state = reconcileControllerState.reconcile(
                internalId,
                teacher.teacherAccountId(),
                teacher.teacherBrowserSessionId(),
                participantCookies.readParticipantCredential(request));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new ControllerReconciliationResponse(state));
    }

    @PostMapping(
            path = "/api/classroom-sessions/{classroomSessionId}/commands",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<ClassroomCommandResponse> command(
            @PathVariable String classroomSessionId,
            @AuthenticationPrincipal(expression = "authenticatedSession")
                    AuthenticatedTeacherSession authenticatedSession,
            @RequestHeader(name = CSRF_HEADER, required = false) String csrfHeader,
            @RequestBody JsonNode requestBody,
            HttpServletRequest request) {
        UUID internalId = parseClassroomSessionId(classroomSessionId);
        ValidatedCommand command = validateCommand(requestBody);
        AuthorizedTeacherMutation teacher = authorizeTeacherMutation.authorizeMutation(
                authenticatedSession, parseToken(csrfHeader));
        var result = executeCommand.execute(new ClassroomCommandRequest(
                internalId,
                command.commandId(),
                command.expectedRevision(),
                teacher.teacherAccountId(),
                teacher.teacherBrowserSessionId(),
                participantCookies.readParticipantCredential(request)));
        return ResponseEntity.ok(new ClassroomCommandResponse(result));
    }

    private static UUID parseClassroomSessionId(String value) {
        validateOpaqueId(value, "classroomSessionId");
        try {
            UUID parsed = UUID.fromString(value);
            if (!parsed.toString().equalsIgnoreCase(value)) {
                throw new ClassroomSessionNotFoundException();
            }
            return parsed;
        } catch (IllegalArgumentException exception) {
            throw new ClassroomSessionNotFoundException();
        }
    }

    private static ValidatedCommand validateCommand(JsonNode body) {
        if (!body.isObject()) {
            throw invalid("request", "INVALID_VALUE", "The request body must be a JSON object.");
        }
        for (String field : List.of("commandId", "expectedRevision", "commandType", "action")) {
            if (!body.has(field)) {
                throw invalid(field, "REQUIRED", "The field is required.");
            }
        }
        if (body.size() != 4) {
            throw invalid("request", "ADDITIONAL_PROPERTY", "The request contains an unsupported property.");
        }
        JsonNode commandIdNode = body.get("commandId");
        if (commandIdNode == null || !commandIdNode.isString()) {
            throw invalidValue("commandId");
        }
        String commandId = commandIdNode.stringValue();
        validateOpaqueId(commandId, "commandId");

        JsonNode revisionNode = body.get("expectedRevision");
        if (revisionNode == null || !revisionNode.isIntegralNumber()) {
            throw invalidValue("expectedRevision");
        }
        BigInteger revision = revisionNode.bigIntegerValue();
        if (revision.signum() < 0 || revision.compareTo(MAX_REVISION) > 0) {
            throw invalidValue("expectedRevision");
        }
        requireSupported(body.get("commandType"), "commandType", "DIRECT_ACTION");
        requireSupported(body.get("action"), "action", "NEXT");
        return new ValidatedCommand(commandId, revision.longValueExact());
    }

    private static void requireSupported(JsonNode node, String field, String supported) {
        if (node == null || !node.isString()) {
            throw invalidValue(field);
        }
        if (!supported.equals(node.stringValue())) {
            throw invalid(field, "UNSUPPORTED_VALUE", "The field value is not supported by this contract.");
        }
    }

    private static void validateOpaqueId(String value, String field) {
        int length = value == null ? 0 : value.codePointCount(0, value.length());
        if (length < 1
                || length > MAX_ID_CODE_POINTS
                || value.codePoints().anyMatch(ClassroomControllerCommandController::isWhitespace)) {
            throw invalidValue(field);
        }
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }

    private static Optional<RawSecurityToken> parseToken(String value) {
        try {
            return Optional.ofNullable(value).map(RawSecurityToken::fromEncoded);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static ClassroomRequestValidationException invalidValue(String field) {
        return invalid(field, "INVALID_VALUE", "The field value does not satisfy its contract.");
    }

    private static ClassroomRequestValidationException invalid(
            String field, String code, String message) {
        return new ClassroomRequestValidationException(
                List.of(new RequestValidationFieldError(field, code, message)));
    }

    private record ValidatedCommand(String commandId, long expectedRevision) {}
}
