package io.github.sipratama.penatika.classroom.adapter.in.http;

import java.util.List;

import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayProjection;

public record ClassroomDisplaySnapshotResponse(
        String schemaVersion,
        String classroomSessionId,
        long revision,
        Scene scene) {

    ClassroomDisplaySnapshotResponse(ClassroomDisplayProjection projection) {
        this(
                projection.schemaVersion(),
                projection.classroomSessionId(),
                projection.revision(),
                new Scene(
                        projection.scene().sceneId(),
                        projection.scene().position(),
                        projection.scene().blocks().stream()
                                .map(block -> new Block(block.type(), block.text()))
                                .toList()));
    }

    public record Scene(String sceneId, long position, List<Block> blocks) {}

    public record Block(String type, String text) {}
}
