package devkn.jtools.smimegen;

import devkn.jtools.mime.MimeMessages;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static devkn.jtools.test.TestResources.getResourceUri;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SMimeGeneratorCliTest {

    @Test
    void mainTest() throws Exception {
        URI signingX509Uri = getResourceUri("/certs/brian.kavanagh@homeaffairs.gov.au.cer");
        URI signingPkcs8Uri = getResourceUri("/certs/brian.kavanagh@homeaffairs.gov.au.encrypted.pkcs8");
        URI encryptX509Uri = getResourceUri("/certs/edi-access-E1.cer");
        URI inputDirUri = getResourceUri("/cli/input");
        URI outputDirUri = getResourceUri("/cli/output");
        String request = """
                {
                  "signingX509Uri": "%s",
                  "signingPkcs8Uri": "%s",
                  "signingPkcs8Password": "password",
                  "encryptX509Uri": "%s",
                  "mimeMessagesDirUri": "%s",
                  "outputDirUri": "%s"
                }
                """.formatted(signingX509Uri, signingPkcs8Uri, encryptX509Uri, inputDirUri, outputDirUri);

        SMimeGeneratorCli.main(new String[]{request});

        Path outputPath1 = Path.of(getResourceUri("/cli/output/mime1"));
        assertThat(Files.isRegularFile(outputPath1)).isTrue();
        // Convert the mime file back to MimeMessage
        MimeMessage smime = MimeMessages.fromString(Files.readString(outputPath1));
        assertThat(smime.getContentType()).startsWith("application/pkcs7-mime");
    }
}


