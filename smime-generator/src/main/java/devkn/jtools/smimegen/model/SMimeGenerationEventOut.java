package devkn.jtools.smimegen.model;

import devkn.jtools.Exceptions;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public record SMimeGenerationEventOut(
        String status,
        URI inputUri,
        URI outputUri,
        String errorMessage,
        List<String> errorTrace
) {

    public static SMimeGenerationEventOut success(Path input, Path output) {
        return new SMimeGenerationEventOut("SUCCESS", input.toUri(), output.toUri(), null, null);
    }

    public static SMimeGenerationEventOut error(Path input, Exception e) {
        return new SMimeGenerationEventOut("ERROR", input.toUri(), null, e.getMessage(), Exceptions.printStackTraceAsList(e));
    }
}
