package io.github.sipratama.penatika.classroom.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.github.sipratama.penatika.classroom.fixtures.AcceptedCommandOutcomeFixtureBuilder;

class AcceptedCommandOutcomeTest {

    private static final String SUPPLEMENTARY_CHARACTER = "\uD83D\uDE00";

    @Test
    void acceptsAsciiOneCodePointAndTheExactUnicodeCodePointLimit() {
        assertThatCode(() -> outcome("command-001")).doesNotThrowAnyException();
        assertThatCode(() -> outcome("x")).doesNotThrowAnyException();

        String exactLimit = SUPPLEMENTARY_CHARACTER.repeat(128);
        assertThatCode(() -> outcome(exactLimit)).doesNotThrowAnyException();
    }

    @Test
    void rejectsCommandIdsAboveTheUnicodeCodePointLimit() {
        String aboveLimit = SUPPLEMENTARY_CHARACTER.repeat(129);

        assertThatThrownBy(() -> outcome(aboveLimit))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void supplementaryCharactersCountAsOneCodePointRatherThanTwoUtf16Units() {
        String exactLimit = SUPPLEMENTARY_CHARACTER.repeat(128);

        assertThatCode(() -> outcome(exactLimit)).doesNotThrowAnyException();
    }

    @Test
    void rejectsEmptyEmbeddedLeadingAndTrailingWhitespace() {
        for (String invalid : new String[] {"", "command id", " command", "command ", "command\tvalue"}) {
            assertThatThrownBy(() -> outcome(invalid))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static AcceptedCommandOutcome outcome(String commandId) {
        return new AcceptedCommandOutcomeFixtureBuilder()
                .withCommandId(commandId)
                .build();
    }
}
