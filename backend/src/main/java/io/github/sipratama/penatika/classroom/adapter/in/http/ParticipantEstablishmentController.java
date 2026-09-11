package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.github.sipratama.penatika.classroom.application.model.EstablishedParticipant;
import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishClassroomDisplayParticipantUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishTeacherControllerParticipantUseCase;
import io.github.sipratama.penatika.identity.application.model.AuthenticatedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.AuthorizedTeacherMutation;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.application.port.in.AuthorizeTeacherMutationUseCase;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.JsonNode;

@RestController
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class ParticipantEstablishmentController {

    private static final String CSRF_HEADER = "X-Penatika-CSRF";

    private final AuthorizeTeacherMutationUseCase authorizeTeacherMutation;
    private final EstablishTeacherControllerParticipantUseCase establishController;
    private final EstablishClassroomDisplayParticipantUseCase establishDisplay;
    private final ParticipantSessionCookies participantCookies;
    private final Clock clock;

    public ParticipantEstablishmentController(
            AuthorizeTeacherMutationUseCase authorizeTeacherMutation,
            EstablishTeacherControllerParticipantUseCase establishController,
            EstablishClassroomDisplayParticipantUseCase establishDisplay,
            ParticipantSessionCookies participantCookies,
            Clock clock) {
        this.authorizeTeacherMutation = authorizeTeacherMutation;
        this.establishController = establishController;
        this.establishDisplay = establishDisplay;
        this.participantCookies = participantCookies;
        this.clock = clock;
    }

    @PostMapping(
            path = "/api/teacher-controller-participants",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<ParticipantEstablishmentResponse> establishController(
            @AuthenticationPrincipal(expression = "authenticatedSession")
                    AuthenticatedTeacherSession authenticatedSession,
            @RequestHeader(name = CSRF_HEADER, required = false) String csrfHeader,
            @RequestBody JsonNode requestBody,
            HttpServletResponse response) {
        PresentedPairingToken token = validateRedemptionRequest(requestBody);
        AuthorizedTeacherMutation teacher = authorizeTeacherMutation.authorizeMutation(
                authenticatedSession, parseCsrfToken(csrfHeader));
        EstablishedParticipant participant = establishController.establishController(
                token, teacher.teacherAccountId(), teacher.teacherBrowserSessionId());
        participantCookies.setParticipantCookie(
                response, participant.participantCredential(), participant.expiresAt(), clock.instant());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ParticipantEstablishmentResponse(
                        participant.classroomSessionId(), participant.participantRole()));
    }

    @PostMapping(
            path = "/api/classroom-display-participants",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<ParticipantEstablishmentResponse> establishDisplay(
            @RequestBody JsonNode requestBody,
            HttpServletResponse response) {
        EstablishedParticipant participant = establishDisplay.establishDisplay(
                validateRedemptionRequest(requestBody));
        participantCookies.setParticipantCookie(
                response, participant.participantCredential(), participant.expiresAt(), clock.instant());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ParticipantEstablishmentResponse(
                        participant.classroomSessionId(), participant.participantRole()));
    }

    private static PresentedPairingToken validateRedemptionRequest(JsonNode requestBody) {
        if (!requestBody.isObject()) {
            throw invalidRequest("INVALID_VALUE", "The request body must be a JSON object.");
        }
        if (!requestBody.has("pairingToken")) {
            throw invalidField("pairingToken", "REQUIRED", "The field is required.");
        }
        if (requestBody.size() != 1) {
            throw invalidRequest("ADDITIONAL_PROPERTY", "The request contains an unsupported property.");
        }
        JsonNode pairingToken = requestBody.get("pairingToken");
        if (pairingToken == null || !pairingToken.isString()) {
            throw invalidField(
                    "pairingToken", "INVALID_VALUE", "The field value does not satisfy its contract.");
        }
        try {
            return PresentedPairingToken.fromWire(pairingToken.stringValue());
        } catch (IllegalArgumentException exception) {
            throw invalidField(
                    "pairingToken", "INVALID_VALUE", "The field value does not satisfy its contract.");
        }
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
