package devkn.jtools.smimegen;

import devkn.jtools.mime.MimeMessages;
import devkn.jtools.smimegen.model.MimeMessageInput;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static devkn.jtools.test.TestResources.getResourcePath;
import static org.assertj.core.api.Assertions.assertThat;

class SMimeGeneratorTest {

    @Test
    void smimeSignAndEncryptTest() throws Exception {
        Path signingX509Path = getResourcePath("/certs/brian.kavanagh@homeaffairs.gov.au.cer");
        Path signingPrivateKeyPath = getResourcePath("/certs/brian.kavanagh@homeaffairs.gov.au.encrypted.pkcs8");
        Path encryptX509Path = getResourcePath("/certs/edi-access-E1.cer");
        SMimeGenerator it = new SMimeGenerator(
                signingX509Path,
                signingPrivateKeyPath,
                "password".toCharArray(),
                encryptX509Path
        );
        MimeMessageInput input = new MimeMessageInput(
                "k@mail.com",
                "mm@mail.com",
                "Mock subject",
                "mock-attachement-name",
                "mock attachment content",
                "application/EDIFACT"
        );
        MimeMessage mime = input.toMimeMessage(Session.getDefaultInstance(new Properties()));
        System.out.println("###mime: \n" + MimeMessages.toString(mime));
        // Signed
        MimeMessage signedMime = it.sign(mime);
        assertThat(signedMime.getContentType()).startsWith("multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha-256;");

        String signedText = MimeMessages.toString(signedMime);
        System.out.println("###signed: \n" + signedText);
        assertThat(signedText).contains("Content-Type: application/EDIFACT; name=mock-attachement-name");
        assertThat(signedText).contains("Content-Disposition: attachment; filename=mock-attachement-name");

        // Encrypted
        MimeMessage encryptedMime = it.encrypt(signedMime);
        assertThat(encryptedMime.getContentType()).startsWith("application/pkcs7-mime;");

        String smimeText = MimeMessages.toString(encryptedMime);
        System.out.println("###smime: \n" + smimeText);
        assertThat(smimeText).contains("Content-Type: application/pkcs7-mime;");
        assertThat(smimeText).contains("Content-Transfer-Encoding: base64");
        assertThat(smimeText).doesNotContain("Content-Transfer-Encoding: 7bit\n\nContent-Type: application/pkcs7-mime;");
    }
}


