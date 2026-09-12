package io.github.sipratama.penatika.classroom.adapter.out.synchronization;

import java.util.List;

import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayProjection;

/**
 * The exact JSON shape validated by {@code classroom-display-projection.schema.json}, used as
 * the SSE {@code data} payload for every state-bearing {@code display-projection} event.
 */
public record DisplayProjectionWireModel(
        String schemaVersion,
        String classroomSessionId,
        long revision,
        Scene scene) {

    public DisplayProjectionWireModel(ClassroomDisplayProjection projection) {
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
