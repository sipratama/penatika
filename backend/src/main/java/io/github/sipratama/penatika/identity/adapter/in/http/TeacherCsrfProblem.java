package io.github.sipratama.penatika.identity.adapter.in.http;

public final class TeacherCsrfProblem {

    public String getType() {
        return "about:blank";
    }

    public String getTitle() {
        return "Forbidden";
    }

    public int getStatus() {
        return 403;
    }

    public String getDetail() {
        return "The request did not include valid CSRF protection.";
    }

    public String getCode() {
        return "CSRF_REJECTED";
    }
}
