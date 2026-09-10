package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.Objects;

public record RequestValidationFieldError(String field, String code, String message) {

    public RequestValidationFieldError {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
