package io.github.sipratama.penatika.lesson.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class ClassroomLessonSceneTest {

    private static final String SCENE_ID = "20000000-0000-0000-0000-000000000701";

    @Test
    void acceptsCanonicalUuidDerivedSceneId() {
        var scene = new ClassroomLessonScene(
                SCENE_ID,
                1,
                List.of(new ClassroomLessonSceneBlock("PLAIN_TEXT", "Classroom text")));

        assertThat(scene.sceneId()).isEqualTo(SCENE_ID);
    }

    @Test
    void rejectsNoBreakSpaceInSceneId() {
        var blocks = List.of(new ClassroomLessonSceneBlock("PLAIN_TEXT", "Classroom text"));

        assertThatThrownBy(() -> new ClassroomLessonScene("scene\u00A0id", 1, blocks))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
