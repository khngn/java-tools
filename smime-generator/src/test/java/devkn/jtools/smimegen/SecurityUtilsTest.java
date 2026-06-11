package devkn.jtools.smimegen;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import static devkn.jtools.test.TestResources.getResourcePath;
import static devkn.jtools.test.TestResources.readResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    @Test
    void readX509CertificateTest() throws Exception {
        Path certPath = getResourcePath("/certs/brian.kavanagh@homeaffairs.gov.au.cer");
        Certificate cert = SecurityUtils.readX509Certificate(certPath);
        assertThat(cert.getType()).isEqualTo("X.509");
    }

    @Test
    void readPrivateKeyTest() throws Exception {
        Path privateKeyPath = getResourcePath("/certs/brian.kavanagh@homeaffairs.gov.au.encrypted.pkcs8");
        PrivateKey privateKey = SecurityUtils.readPrivateKey(privateKeyPath, "password".toCharArray());
        assertThat(privateKey.getFormat()).isEqualTo("PKCS#8");
    }
}


