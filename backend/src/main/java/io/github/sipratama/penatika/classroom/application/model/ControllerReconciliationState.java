package io.github.sipratama.penatika.classroom.application.model;

import java.util.Objects;

public record ControllerReconciliationState(String classroomSessionId, long revision) {

    public ControllerReconciliationState {
        Objects.requireNonNull(classroomSessionId, "classroomSessionId must not be null");
    }
}
