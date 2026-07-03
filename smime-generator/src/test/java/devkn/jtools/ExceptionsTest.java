package devkn.jtools;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    void printStackTraceAsList_replacesTabsWithFourSpaces() {
        RuntimeException ex = new RuntimeException("boom");

        List<String> lines = Exceptions.printStackTraceAsList(ex);

        assertThat(lines).isNotEmpty();
        assertThat(lines.stream().anyMatch(line -> line.contains("\t"))).isFalse();
        assertThat(lines.stream().anyMatch(line -> line.startsWith("    at "))).isTrue();
    }
}

