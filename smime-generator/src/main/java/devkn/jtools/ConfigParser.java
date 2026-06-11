package devkn.jtools;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ConfigParser {

    private static final String CONTENT_PREFIX = "CONTENT:";
    private static final String URI_PREFIX = "URI:";
    private static final String CONTENT_URI_PREFIX = CONTENT_PREFIX + URI_PREFIX;

    private ConfigParser() {}

    public static String parse(String value) {
        if (value.startsWith(URI_PREFIX)) {
            URI uri = URI.create(value.substring(URI_PREFIX.length()));
            // Should be file URI
            Path path = Path.of(uri);
            checkArgument(Files.exists(path), "File does not exist: " + uri);
            return path.toAbsolutePath().toString();
        } else if (value.startsWith(CONTENT_URI_PREFIX)) {
            URI uri = URI.create(value.substring(CONTENT_URI_PREFIX.length()));
            Path path = Path.of(uri);
            try {
                return Files.readString(path, UTF_8);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read content of: " + uri, e);
            }
        }
        return value;
    }
}
