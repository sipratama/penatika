package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;
import java.util.Objects;

public final class ClassroomRequestValidationException extends RuntimeException {

    private final List<RequestValidationFieldError> fieldErrors;

    public ClassroomRequestValidationException(List<RequestValidationFieldError> fieldErrors) {
        super("The Classroom Session request failed structural validation");
        this.fieldErrors = List.copyOf(Objects.requireNonNull(fieldErrors, "fieldErrors must not be null"));
        if (this.fieldErrors.isEmpty()) {
            throw new IllegalArgumentException("fieldErrors must not be empty");
        }
    }

    public List<RequestValidationFieldError> fieldErrors() {
        return fieldErrors;
    }
}
