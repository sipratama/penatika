package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.sipratama.penatika.classroom.application.port.in.GetClassroomDisplaySnapshotUseCase;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class ClassroomDisplaySnapshotController {

    private static final int MAX_ID_CODE_POINTS = 128;

    private final GetClassroomDisplaySnapshotUseCase getSnapshot;
    private final ParticipantSessionCookies participantCookies;

    public ClassroomDisplaySnapshotController(
            GetClassroomDisplaySnapshotUseCase getSnapshot,
            ParticipantSessionCookies participantCookies) {
        this.getSnapshot = getSnapshot;
        this.participantCookies = participantCookies;
    }

    @GetMapping(
            path = "/api/classroom-sessions/{classroomSessionId}/display-snapshot",
            produces = "application/json")
    public ResponseEntity<ClassroomDisplaySnapshotResponse> getSnapshot(
            @PathVariable String classroomSessionId,
            HttpServletRequest request) {
        validateOpaqueId(classroomSessionId);
        var projection = getSnapshot.getSnapshot(
                classroomSessionId, participantCookies.readParticipantCredential(request));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new ClassroomDisplaySnapshotResponse(projection));
    }

    private static void validateOpaqueId(String value) {
        int length = value == null ? 0 : value.codePointCount(0, value.length());
        if (length < 1
                || length > MAX_ID_CODE_POINTS
                || value.codePoints().anyMatch(Character::isWhitespace)) {
            throw new ClassroomRequestValidationException(List.of(new RequestValidationFieldError(
                    "classroomSessionId",
                    "INVALID_VALUE",
                    "The field must be a non-whitespace opaque identifier of at most 128 characters.")));
        }
    }
}
