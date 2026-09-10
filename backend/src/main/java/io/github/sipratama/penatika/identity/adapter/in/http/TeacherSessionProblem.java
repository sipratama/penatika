package io.github.sipratama.penatika.identity.adapter.in.http;

public final class TeacherSessionProblem {

    public String getType() {
        return "about:blank";
    }

    public String getTitle() {
        return "Unauthorized";
    }

    public int getStatus() {
        return 401;
    }

    public String getDetail() {
        return "An authenticated Teacher session is required.";
    }

    public String getCode() {
        return "TEACHER_SESSION_REQUIRED";
    }
}
