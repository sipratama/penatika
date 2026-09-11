package io.github.sipratama.penatika.classroom.adapter.in.http;

import io.github.sipratama.penatika.classroom.application.model.ControllerReconciliationState;

public record ControllerReconciliationResponse(String classroomSessionId, long revision) {

    public ControllerReconciliationResponse(ControllerReconciliationState state) {
        this(state.classroomSessionId(), state.revision());
    }
}
