package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;

public final class RequestValidationProblem {

    private final List<RequestValidationFieldError> fieldErrors;

    public RequestValidationProblem(List<RequestValidationFieldError> fieldErrors) {
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public String getType() {
        return "about:blank";
    }

    public String getTitle() {
        return "Bad Request";
    }

    public int getStatus() {
        return 400;
    }

    public String getDetail() {
        return "One or more request fields are invalid.";
    }

    public String getCode() {
        return "REQUEST_VALIDATION_FAILED";
    }

    public List<RequestValidationFieldError> getFieldErrors() {
        return fieldErrors;
    }
}
