package devkn.jtools.mime;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MimeMessages {
    private MimeMessages() {}

    public static String toString(MimeMessage mime) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            mime.writeTo(out);
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read MimeMessage", e);
        }
    }

    public static MimeMessage fromString(String mimeString) {
        Session session = Session.getDefaultInstance(new Properties());
        try {
            return new MimeMessage(session, new ByteArrayInputStream(mimeString.getBytes(StandardCharsets.UTF_8)));
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to parse MimeMessage from string", e);
        }
    }
}
