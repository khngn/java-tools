package devkn.jtools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JPaths {
    private JPaths() {}

    public static String fileNameWithoutExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? name : name.substring(0, dot);
    }

    public static void ensureReadableDirectory(Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path is NOT a directory: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("Directory is NOT readable: " + path);
        }
    }

    public static void ensureWritableDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to create directory at path: " + dir, e);
            }
        }

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path is NOT a directory: " + dir);
        }

        try {
            Path probe = Files.createTempFile(dir, "_probe", ".tmp");
            Files.deleteIfExists(probe);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to create file in dir path: " + dir, e);
        }
    }

}
