package devkn.jtools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class Exceptions {
    private Exceptions() {}

    public static List<String> printStackTraceAsList(Throwable e) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            pw.flush();
        }
        String normalized = sw.toString().replace("\t", "    ");
        return Arrays.asList(normalized.split("\\R"));
    }
}
