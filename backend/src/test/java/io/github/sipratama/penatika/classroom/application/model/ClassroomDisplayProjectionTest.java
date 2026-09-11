package io.github.sipratama.penatika.classroom.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ClassroomDisplayProjectionTest {

    @Test
    void plainTextBoundsCountSupplementaryUnicodeAsOneCodePoint() {
        String exact = "\uD83E\uDDEE".repeat(16_384);

        ClassroomDisplayBlock block = new ClassroomDisplayBlock("PLAIN_TEXT", exact);

        assertThat(block.text()).hasSize(32_768);
        assertThat(block.text().codePointCount(0, block.text().length())).isEqualTo(16_384);
        assertThatThrownBy(() -> new ClassroomDisplayBlock("PLAIN_TEXT", exact + "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyTextAndUnsupportedBlockType() {
        assertThatThrownBy(() -> new ClassroomDisplayBlock("PLAIN_TEXT", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClassroomDisplayBlock("HTML", "text"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
