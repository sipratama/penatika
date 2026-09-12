package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.math.BigInteger;
import java.time.Clock;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.github.sipratama.penatika.classroom.adapter.out.synchronization.DisplayProjectionWireModel;
import io.github.sipratama.penatika.classroom.adapter.out.synchronization.DisplayStream;
import io.github.sipratama.penatika.classroom.adapter.out.synchronization.DisplayStreamRegistry;
import io.github.sipratama.penatika.classroom.application.model.EstablishedDisplayStream;
import io.github.sipratama.penatika.classroom.application.port.in.AcknowledgeDisplaySynchronizationUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishClassroomDisplayStreamUseCase;
import io.github.sipratama.penatika.classroom.domain.Revision;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.JsonNode;

@RestController
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class ClassroomDisplayStreamController {

    private static final int MAX_ID_CODE_POINTS = 128;
    private static final BigInteger MAX_REVISION = BigInteger.valueOf(Revision.MAX_VALUE);
    private static final String DISPLAY_INTENT_HEADER = "X-Penatika-Display-Intent";
    private static final String DISPLAY_INTENT_VALUE = "synchronize";

    private final EstablishClassroomDisplayStreamUseCase establishStream;
    private final AcknowledgeDisplaySynchronizationUseCase acknowledgeSynchronization;
    private final ParticipantSessionCookies participantCookies;
    private final DisplayStreamRegistry registry;
    private final Clock clock;

    public ClassroomDisplayStreamController(
            EstablishClassroomDisplayStreamUseCase establishStream,
            AcknowledgeDisplaySynchronizationUseCase acknowledgeSynchronization,
            ParticipantSessionCookies participantCookies,
            DisplayStreamRegistry registry,
            Clock clock) {
        this.establishStream = establishStream;
        this.acknowledgeSynchronization = acknowledgeSynchronization;
        this.participantCookies = participantCookies;
        this.registry = registry;
        this.clock = clock;
    }

    @GetMapping(
            path = "/api/classroom-sessions/{classroomSessionId}/display-events",
            produces = "text/event-stream")
    public SseEmitter streamDisplayEvents(
            @PathVariable String classroomSessionId,
            @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response) {
        validateOpaqueId(classroomSessionId, "classroomSessionId");
        validateLastEventId(lastEventId);

        EstablishedDisplayStream established = establishStream.establish(
                classroomSessionId, participantCookies.readParticipantCredential(request));

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        SseEmitter emitter = new SseEmitter(0L);
        java.util.UUID internalId = established.classroomSessionId().value();
        DisplayStream stream = registry.register(
                internalId, established.participantSessionId(), emitter, clock.instant());

        emitter.onCompletion(() -> cleanup(internalId, stream));
        emitter.onTimeout(() -> cleanup(internalId, stream));
        emitter.onError(throwable -> cleanup(internalId, stream));

        boolean sent = stream.sendProjection(
                established.projection().revision(),
                new DisplayProjectionWireModel(established.projection()),
                clock.instant());
        if (!sent) {
            cleanup(internalId, stream);
        }
        return emitter;
    }

    @PutMapping(
            path = "/api/classroom-sessions/{classroomSessionId}/display-synchronization",
            consumes = "application/json")
    public ResponseEntity<Void> acknowledgeSynchronization(
            @PathVariable String classroomSessionId,
            @RequestHeader(name = DISPLAY_INTENT_HEADER, required = false) String displayIntent,
            @RequestBody JsonNode requestBody,
            HttpServletRequest request) {
        validateOpaqueId(classroomSessionId, "classroomSessionId");
        long revision = validateSynchronizationBody(requestBody);
        boolean validIntent = DISPLAY_INTENT_VALUE.equals(displayIntent);
        acknowledgeSynchronization.acknowledge(
                classroomSessionId,
                participantCookies.readParticipantCredential(request),
                validIntent,
                revision);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void cleanup(java.util.UUID classroomSessionId, DisplayStream stream) {
        registry.unregisterIfCurrent(classroomSessionId, stream);
        stream.terminate();
    }

    private static void validateLastEventId(String value) {
        if (value == null) {
            return;
        }
        BigInteger parsed;
        try {
            parsed = new BigInteger(value.trim());
        } catch (NumberFormatException exception) {
            throw invalidValue("Last-Event-ID");
        }
        if (!value.trim().equals(parsed.toString()) || parsed.signum() < 0 || parsed.compareTo(MAX_REVISION) > 0) {
            throw invalidValue("Last-Event-ID");
        }
    }

    private static long validateSynchronizationBody(JsonNode body) {
        if (!body.isObject()) {
            throw invalid("request", "INVALID_VALUE", "The request body must be a JSON object.");
        }
        if (!body.has("revision")) {
            throw invalid("revision", "REQUIRED", "The field is required.");
        }
        if (body.size() != 1) {
            throw invalid("request", "ADDITIONAL_PROPERTY", "The request contains an unsupported property.");
        }
        JsonNode revisionNode = body.get("revision");
        if (revisionNode == null || !revisionNode.isIntegralNumber()) {
            throw invalidValue("revision");
        }
        BigInteger revision = revisionNode.bigIntegerValue();
        if (revision.signum() < 0 || revision.compareTo(MAX_REVISION) > 0) {
            throw invalidValue("revision");
        }
        return revision.longValueExact();
    }

    private static void validateOpaqueId(String value, String field) {
        int length = value == null ? 0 : value.codePointCount(0, value.length());
        if (length < 1
                || length > MAX_ID_CODE_POINTS
                || value.codePoints().anyMatch(ClassroomDisplayStreamController::isWhitespace)) {
            throw invalidValue(field);
        }
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }

    private static ClassroomRequestValidationException invalidValue(String field) {
        return invalid(field, "INVALID_VALUE", "The field value does not satisfy its contract.");
    }

    private static ClassroomRequestValidationException invalid(String field, String code, String message) {
        return new ClassroomRequestValidationException(
                List.of(new RequestValidationFieldError(field, code, message)));
    }
}
