package io.github.sipratama.penatika.lesson.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class SceneBlockTest {

    private static final int MAX_PLAIN_TEXT_LENGTH = 16_384;
    private static final String SUPPLEMENTARY_CHARACTER = "\uD83D\uDE00";
    private static final SceneBlockId BLOCK_ID =
            new SceneBlockId(UUID.fromString("20000000-0000-0000-0000-000000000501"));
    private static final LessonSceneId SCENE_ID =
            new LessonSceneId(UUID.fromString("20000000-0000-0000-0000-000000000502"));

    @Test
    void acceptsOrdinaryAsciiText() {
        assertThatCode(() -> block("Classroom-ready plain text")).doesNotThrowAnyException();
    }

    @Test
    void rejectsEmptyText() {
        assertThatThrownBy(() -> block(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plainText length must be between 1 and 16384");
    }

    @Test
    void acceptsExactlyMaximumUnicodeCodePoints() {
        String text = SUPPLEMENTARY_CHARACTER.repeat(MAX_PLAIN_TEXT_LENGTH);

        assertThat(text).hasSize(MAX_PLAIN_TEXT_LENGTH * 2);
        assertThat(text.codePointCount(0, text.length())).isEqualTo(MAX_PLAIN_TEXT_LENGTH);
        assertThatCode(() -> block(text)).doesNotThrowAnyException();
    }

    @Test
    void rejectsOneUnicodeCodePointAboveMaximum() {
        String text = SUPPLEMENTARY_CHARACTER.repeat(MAX_PLAIN_TEXT_LENGTH + 1);

        assertThat(text.codePointCount(0, text.length())).isEqualTo(MAX_PLAIN_TEXT_LENGTH + 1);
        assertThatThrownBy(() -> block(text))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plainText length must be between 1 and 16384");
    }

    @Test
    void countsEachSupplementaryCharacterAsOneCodePoint() {
        String text = SUPPLEMENTARY_CHARACTER.repeat((MAX_PLAIN_TEXT_LENGTH / 2) + 1);

        assertThat(text.length()).isGreaterThan(MAX_PLAIN_TEXT_LENGTH);
        assertThat(text.codePointCount(0, text.length())).isLessThan(MAX_PLAIN_TEXT_LENGTH);
        assertThatCode(() -> block(text)).doesNotThrowAnyException();
    }

    private static SceneBlock block(String plainText) {
        return new SceneBlock(
                BLOCK_ID,
                SCENE_ID,
                0,
                SceneBlockType.PLAIN_TEXT,
                plainText);
    }
}
