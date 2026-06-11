package devkn.jtools.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TestResources {

    public static String readResourceAsString(String resourceName) {
        try (InputStream in = TestResources.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static URI getResourceUri(String resourceName) {
        URL url = TestResources.class.getResource(resourceName);
        if (url == null) throw new IllegalArgumentException("Resource not found: " + resourceName);
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getResourcePath(String resourceName) {
        URI uri = getResourceUri(resourceName);
        return Path.of(uri);
    }

}
