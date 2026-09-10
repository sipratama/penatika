package io.github.sipratama.penatika.classroom.adapter.in.http;

public final class ClassroomSessionProblem {

    private final String title;
    private final int status;
    private final String detail;
    private final String code;

    public ClassroomSessionProblem(String title, int status, String detail, String code) {
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.code = code;
    }

    public String getType() {
        return "about:blank";
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getCode() {
        return code;
    }
}
