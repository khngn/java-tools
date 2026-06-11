package devkn.jtools;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static devkn.jtools.test.TestResources.getResourceUri;
import static org.assertj.core.api.Assertions.assertThat;

class ConfigParserTest {

    @Test
    void parseNoop() {
        assertThat(ConfigParser.parse("mock")).isEqualTo("mock");
    }

    @Test
    void parseUriTest() {
        URI uri = getResourceUri("/parser/parser-test-file");
        String filePath = ConfigParser.parse("URI:" + uri);
        File file = new File(filePath);
        assertThat(file.exists()).isTrue();
        assertThat(file.getName()).isEqualTo("parser-test-file");
    }

    @Test
    void parseContentUriTest() {
        URI uri = getResourceUri("/parser/parser-test-file");
        String content = ConfigParser.parse("CONTENT:URI:" + uri);
        assertThat(content).isEqualTo("content-of-parser-test-file");
    }

}