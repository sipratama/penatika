package io.github.sipratama.penatika.classroom.adapter.in.http;

import io.github.sipratama.penatika.classroom.application.model.StartedClassroomSession;

public final class StartClassroomSessionResponse {

    private final String classroomSessionId;
    private final String lessonVersionId;
    private final long revision;

    public StartClassroomSessionResponse(StartedClassroomSession session) {
        this.classroomSessionId = session.classroomSessionId();
        this.lessonVersionId = session.lessonVersionId();
        this.revision = session.revision();
    }

    public String getClassroomSessionId() {
        return classroomSessionId;
    }

    public String getLessonVersionId() {
        return lessonVersionId;
    }

    public long getRevision() {
        return revision;
    }
}
