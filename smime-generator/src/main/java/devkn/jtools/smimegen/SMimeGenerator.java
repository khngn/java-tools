package devkn.jtools.smimegen;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;

import java.io.IOException;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static devkn.jtools.smimegen.SecurityUtils.readPrivateKey;
import static devkn.jtools.smimegen.SecurityUtils.readX509Certificate;

public class SMimeGenerator {

    private static final String SIGNATURE_ALGORITHM = "SHA256WITHRSA";
    private static final ASN1ObjectIdentifier ENCRYPTION_ALGORITHM = CMSAlgorithm.AES128_CBC;

    private final SMIMESignedGenerator signedGenerator;

    private final SMIMEEnvelopedGenerator smimeEnvelopedGenerator;
    private final OutputEncryptor encryptor;

    public SMimeGenerator(
            Path signingX509,
            Path signingPkcs8,
            char[] signingPkcs8Password,
            Path encryptX509
    ) {
        X509Certificate signingCertificate = readX509Certificate(signingX509);
        PrivateKey signingPrivateKey = readPrivateKey(signingPkcs8, signingPkcs8Password);

        // Signer
        signedGenerator = new SMIMESignedGenerator();
        try {
            signedGenerator.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
                    .build(SIGNATURE_ALGORITHM, signingPrivateKey, signingCertificate));
        } catch (OperatorCreationException | CertificateEncodingException e) {
            throw new RuntimeException("Failed to init Signer", e);
        }

        // Encryptor
        X509Certificate encryptCertificate = readX509Certificate(encryptX509);
        smimeEnvelopedGenerator = new SMIMEEnvelopedGenerator();
        smimeEnvelopedGenerator.setContentTransferEncoding("base64");
        try {
            smimeEnvelopedGenerator.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(encryptCertificate)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME));
            encryptor = new JceCMSContentEncryptorBuilder(ENCRYPTION_ALGORITHM)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
        } catch (CMSException | CertificateEncodingException e) {
            throw new RuntimeException("Failed to init Encryptor", e);
        }
    }

    private MimeMessage copy(MimeMessage mime) {
        try {
            MimeMessage copy = new MimeMessage(mime.getSession());
            copy.addFrom(mime.getFrom());
            copy.setRecipients(MimeMessage.RecipientType.TO, mime.getRecipients(MimeMessage.RecipientType.TO));
            copy.setRecipients(MimeMessage.RecipientType.CC, mime.getRecipients(MimeMessage.RecipientType.CC));
            copy.setRecipients(MimeMessage.RecipientType.BCC, mime.getRecipients(MimeMessage.RecipientType.BCC));
            copy.setReplyTo(mime.getReplyTo());
            copy.setSubject(mime.getSubject());
            copy.saveChanges();
            return copy;
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to copy MimeMessage", e);
        }
    }

    public MimeMessage sign(MimeMessage mime) {
        try {
            MimeMessage signed = new MimeMessage(mime);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(mime.getContent(), mime.getContentType());
            MimeMultipart signedMultipart = signedGenerator.generate(mimeBodyPart);
            signed.setContent(signedMultipart);
            signed.saveChanges();
            return signed;
        } catch (SMIMEException | MessagingException | IOException e) {
            throw new RuntimeException("Failed to sign MimeMessage", e);
        }
    }

    public MimeMessage encrypt(MimeMessage mime) {
        try {
            MimeMessage smime = new MimeMessage(mime);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(mime.getContent(), mime.getContentType());
            // Encrypt (only) the content of the input message.
            MimeBodyPart smimeBodyPart = smimeEnvelopedGenerator.generate(mimeBodyPart, encryptor);
            smime.removeHeader("Content-Type");
            smime.removeHeader("Content-Transfer-Encoding");
            smime.removeHeader("Content-Disposition");
            smime.removeHeader("Content-Description");

            // Set encrypted payload as the message content, not as a nested MIME part.
            smime.setContent(smimeBodyPart.getContent(), smimeBodyPart.getContentType());
            smime.saveChanges();
            return smime;
        } catch (MessagingException | SMIMEException | IOException e) {
            throw new RuntimeException("Failed to encrypt MimeMessage", e);
        }
    }
}