package devkn.jtools.smimegen.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;
import java.nio.file.Path;

public record MimeMessageInput(
        String toAddress,
        String fromAddress,
        String subject,
        String attachmentName,
        String attachmentContent,
        String attachmentContentType
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static MimeMessageInput fromJson(Path path) {
        try {
            return mapper.readValue(path.toFile(), MimeMessageInput.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public MimeMessage toMimeMessage(Session session) {
        MimeMessage mime = new MimeMessage(session);

        try {
            mime.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            mime.addFrom(new InternetAddress[]{new InternetAddress(fromAddress)});
            mime.setSubject(subject);

            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setFileName(attachmentName);
            attachment.setContent(attachmentContent, attachmentContentType);

            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.addBodyPart(attachment);
            mime.setContent(mimeMultipart);

            return mime;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
