package devkn.jtools.smimegen;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SecurityUtils {
    private SecurityUtils() {
    }

    public static PrivateKey readPrivateKey(Path pkcs8, char[] password) {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        try (Reader pkcs8Reader = Files.newBufferedReader(pkcs8, StandardCharsets.UTF_8);
             PEMParser pemParser = new PEMParser(pkcs8Reader)
        ) {
            PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
            // Read the private key
            JceOpenSSLPKCS8DecryptorProviderBuilder builder = new JceOpenSSLPKCS8DecryptorProviderBuilder();
            InputDecryptorProvider pkcs8InputDecrypter = builder.build(password);
            PrivateKeyInfo decryptedPrivateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(pkcs8InputDecrypter);
            // Convert the "bouncy-castle" private key into a JCA (java Cryptography Architecture) PrivateKey.
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            converter.setProvider(BouncyCastleProvider.PROVIDER_NAME);
            return converter.getPrivateKey(decryptedPrivateKeyInfo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read PrivateKey", e);
        }
    }

    public static X509Certificate readX509Certificate(Path x509) {
        String x509String = null;
        try {
            x509String = Files.readString(x509, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read path: " + x509, e);
        }

        if (x509String.isBlank()) {
            throw new IllegalArgumentException("Certificate string is empty");
        }

        String certString = x509String.trim()
                .replace("-----BEGIN TRUSTED CERTIFICATE-----", "-----BEGIN CERTIFICATE-----")
                .replace("-----END TRUSTED CERTIFICATE-----", "-----END CERTIFICATE-----");

        CertificateFactory certificateFactory = getCertificateFactory("X.509");    // PEM input (with headers)
        // US_ASCII is mainly about correctness-by-intent and clarity: Communicates intent: "this input should only contain ASCII"
        try (InputStream in = new ByteArrayInputStream(certString.getBytes(StandardCharsets.US_ASCII))) {
            return (X509Certificate) certificateFactory.generateCertificate(in);
        } catch (CertificateException | IOException e) {
            throw new IllegalArgumentException("Failed to generate Certificate from X509 string", e);
        }
    }

    public static CertificateFactory getCertificateFactory(String type) {
        try {
            return CertificateFactory.getInstance(type);
        } catch (CertificateException e) {
            throw new IllegalStateException("Failed to create a CertificateFactory instance of type " + type, e);
        }
    }
}
