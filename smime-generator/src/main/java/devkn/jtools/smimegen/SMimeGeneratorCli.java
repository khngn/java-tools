package devkn.jtools.smimegen;

import devkn.jtools.JPaths;
import devkn.jtools.mime.MimeMessages;
import devkn.jtools.smimegen.model.MimeMessageInput;
import devkn.jtools.smimegen.model.SMimeGenerationRequest;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static devkn.jtools.JPaths.ensureReadableDirectory;
import static devkn.jtools.JPaths.ensureWritableDirectory;

public class SMimeGeneratorCli {
    private static final PrettyLogger logger = PrettyLoggerFactory.getLogger(SMimeGeneratorCli.class);

    public static void main(String[] args) {
        checkArgument(args.length == 1, "Expected exactly one argument: JSON string of SMimeGenerationRequest");
        logger.time("main");
        SMimeGenerationRequest request = SMimeGenerationRequest.fromJsonString(args[0]);

        Path inputDir = Path.of(URI.create(request.mimeMessagesDirUri()));
        ensureReadableDirectory(inputDir);

        Path outputDir = Path.of(URI.create(request.outputDirUri()));
        ensureWritableDirectory(outputDir);

        SMimeGenerator sMimeGenerator = new SMimeGenerator(
                Paths.get(URI.create(request.signingX509Uri())),
                Paths.get(URI.create(request.signingPkcs8Uri())),
                request.signingPkcs8Password().toCharArray(),
                Paths.get(URI.create((request.encryptX509Uri())))
        );

        // Read json files in inputDir
        Session session = Session.getDefaultInstance(new Properties());
        try (Stream<Path> paths = Files.list(inputDir)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".json")).forEach(inputJson -> {
                try {
                    MimeMessageInput mimeMessageInput = MimeMessageInput.fromJson(inputJson);
                    MimeMessage mimeMessage = mimeMessageInput.toMimeMessage(session);
                    MimeMessage smimeMessage = sMimeGenerator.encrypt(sMimeGenerator.sign(mimeMessage));
                    // Use input file name sans extension
                    String outName = JPaths.fileNameWithoutExtension(inputJson);
                    Path outputFilePath = outputDir.resolve(outName);
                    // Write to output
                    Files.writeString(outputFilePath, MimeMessages.toString(smimeMessage), StandardCharsets.UTF_8);
                    logger.info("Generated S/MIME message for [{}] at: {}", inputJson, outputFilePath);
                } catch (Exception e) {
                    logger.error("Failed to generate S/MIME message from JSON [{}]:", inputJson, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to list input directory: " + inputDir, e);
        }

        logger.timeEnd("main");
    }
}