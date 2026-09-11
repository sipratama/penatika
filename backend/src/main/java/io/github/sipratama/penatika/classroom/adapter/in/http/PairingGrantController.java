package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.github.sipratama.penatika.classroom.application.model.IssuedPairingGrant;
import io.github.sipratama.penatika.classroom.application.port.in.IssuePairingGrantUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.RevokePairingGrantUseCase;
import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherMutation;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.in.AuthorizeTeacherMutationUseCase;
import tools.jackson.databind.JsonNode;

@RestController
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class PairingGrantController {

    private static final String CSRF_HEADER = "X-Penatika-CSRF";
    private static final int MAX_OPAQUE_ID_CODE_POINTS = 128;
    private static final Pattern NON_WHITESPACE = Pattern.compile("^\\S+$", Pattern.UNICODE_CHARACTER_CLASS);

    private final AuthorizeTeacherMutationUseCase authorizeTeacherMutation;
    private final IssuePairingGrantUseCase issuePairingGrant;
    private final RevokePairingGrantUseCase revokePairingGrant;

    public PairingGrantController(
            AuthorizeTeacherMutationUseCase authorizeTeacherMutation,
            IssuePairingGrantUseCase issuePairingGrant,
            RevokePairingGrantUseCase revokePairingGrant) {
        this.authorizeTeacherMutation = authorizeTeacherMutation;
        this.issuePairingGrant = issuePairingGrant;
        this.revokePairingGrant = revokePairingGrant;
    }

    @PostMapping(
            path = "/api/classroom-sessions/{classroomSessionId}/pairing-grants",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<CreatePairingGrantResponse> create(
            @PathVariable String classroomSessionId,
            @AuthenticationPrincipal(expression = "authenticatedSession")
                    AuthenticatedTeacherSession authenticatedSession,
            @RequestHeader(name = CSRF_HEADER, required = false) String csrfHeader,
            @RequestBody JsonNode requestBody) {
        String validatedSessionId = validateOpaqueId(classroomSessionId, "classroomSessionId");
        String participantRole = validateCreateRequest(requestBody);
        AuthorizedTeacherMutation teacher = authorizeTeacherMutation.authorizeMutation(
                authenticatedSession, parseCsrfToken(csrfHeader));
        IssuedPairingGrant grant = issuePairingGrant.issue(
                validatedSessionId, participantRole, teacher.teacherAccountId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(new CreatePairingGrantResponse(
                        grant.pairingGrantId(),
                        grant.pairingToken().expose(),
                        grant.participantRole(),
                        grant.expiresAt()));
    }

    @DeleteMapping("/api/classroom-sessions/{classroomSessionId}/pairing-grants/{pairingGrantId}")
    public ResponseEntity<Void> revoke(
            @PathVariable String classroomSessionId,
            @PathVariable String pairingGrantId,
            @AuthenticationPrincipal(expression = "authenticatedSession")
                    AuthenticatedTeacherSession authenticatedSession,
            @RequestHeader(name = CSRF_HEADER, required = false) String csrfHeader) {
        String validatedSessionId = validateOpaqueId(classroomSessionId, "classroomSessionId");
        String validatedGrantId = validateOpaqueId(pairingGrantId, "pairingGrantId");
        AuthorizedTeacherMutation teacher = authorizeTeacherMutation.authorizeMutation(
                authenticatedSession, parseCsrfToken(csrfHeader));
        revokePairingGrant.revoke(validatedSessionId, validatedGrantId, teacher.teacherAccountId());
        return ResponseEntity.noContent().build();
    }

    private static String validateCreateRequest(JsonNode requestBody) {
        if (!requestBody.isObject()) {
            throw invalidRequest("INVALID_VALUE", "The request body must be a JSON object.");
        }
        if (!requestBody.has("participantRole")) {
            throw invalidField("participantRole", "REQUIRED", "The field is required.");
        }
        if (requestBody.size() != 1) {
            throw invalidRequest("ADDITIONAL_PROPERTY", "The request contains an unsupported property.");
        }
        JsonNode participantRole = requestBody.get("participantRole");
        if (participantRole == null || !participantRole.isString()) {
            throw invalidField(
                    "participantRole", "INVALID_VALUE", "The field value does not satisfy its contract.");
        }
        String value = participantRole.stringValue();
        if (!"TEACHER_CONTROLLER".equals(value) && !"CLASSROOM_DISPLAY".equals(value)) {
            throw invalidField(
                    "participantRole", "INVALID_VALUE", "The field value does not satisfy its contract.");
        }
        return value;
    }

    private static String validateOpaqueId(String value, String field) {
        int length = value == null ? 0 : value.codePointCount(0, value.length());
        if (length < 1 || length > MAX_OPAQUE_ID_CODE_POINTS || !NON_WHITESPACE.matcher(value).matches()) {
            throw invalidField(field, "INVALID_VALUE", "The field value does not satisfy its contract.");
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

    private static ClassroomRequestValidationException invalidField(
            String field, String code, String message) {
        return new ClassroomRequestValidationException(List.of(
                new RequestValidationFieldError(field, code, message)));
    }

    private static ClassroomRequestValidationException invalidRequest(String code, String message) {
        return invalidField("request", code, message);
    }
}
