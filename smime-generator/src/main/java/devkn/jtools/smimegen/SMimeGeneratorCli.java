package devkn.jtools.smimegen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import devkn.jtools.JPaths;
import devkn.jtools.mime.MimeMessages;
import devkn.jtools.smimegen.model.MimeMessageInput;
import devkn.jtools.smimegen.model.SMimeGenerationEventIn;
import devkn.jtools.smimegen.model.SMimeGenerationEventOut;
import devkn.jtools.smimegen.model.SMimeGenerationRequest;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import static net.logstash.logback.argument.StructuredArguments.kv;

public class SMimeGeneratorCli {
    //private static final PrettyLogger logger = PrettyLoggerFactory.getLogger(SMimeGeneratorCli.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(SMimeGeneratorCli.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

    public static void main(String[] args) {
        LOGGER.info("Starting {}", SMimeGeneratorCli.class.getSimpleName(), kv("args", args));

        checkArgument(args.length == 1, "Expected exactly one argument: JSON string of SMimeGenerationRequest");
        //logger.time("main");
        SMimeGenerationRequest request = SMimeGenerationRequest.fromJsonString(args[0]);

        SMimeGenerator sMimeGenerator = new SMimeGenerator(
                Paths.get(URI.create(request.signingX509Uri())),
                Paths.get(URI.create(request.signingPkcs8Uri())),
                request.signingPkcs8Password().toCharArray(),
                Paths.get(URI.create((request.encryptX509Uri())))
        );
        Session session = Session.getDefaultInstance(new Properties());
        Processor processor = new Processor(sMimeGenerator, session);

        // There are two modes of operation:
        // 1. If both mimeMessagesDirUri and outputDirUri are provided, process all JSON files in the input directory
        //    and write S/MIME messages to the output directory.
        // 2. If either mimeMessagesDirUri or outputDirUri is not provided, read JSON lines from System.in and write
        //    S/MIME messages to the specified output path in each line
        if (request.mimeMessagesDirUri() != null && request.outputDirUri() != null) {
            Path inputDir = Path.of(URI.create(request.mimeMessagesDirUri()));
            ensureReadableDirectory(inputDir);

            Path outputDir = Path.of(URI.create(request.outputDirUri()));
            ensureWritableDirectory(outputDir);

            // Read json files in inputDir
            try (Stream<Path> paths = Files.list(inputDir)) {
                paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".json")).forEach(inputJson -> {
                    // Use inputUri file name sans extension
                    String outName = JPaths.fileNameWithoutExtension(inputJson);
                    Path outputFilePath = outputDir.resolve(outName);
                    processor.process(inputJson, outputFilePath);
                });
            } catch (IOException e) {
                throw new RuntimeException("Failed to list inputUri directory: " + inputDir, e);
            } finally {
                LOGGER.info("Finished processing inputUri directory: {}", inputDir);
            }
        } else {
            // Wrap "System.in" in a BufferedReader to read line-by-line efficiently
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                // This loop blocks and waits until a new line arrives from caller
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    // Parse the incoming JSON line from caller
                    SMimeGenerationEventIn inEvent = SMimeGenerationEventIn.of(line);
                    processor.process(Paths.get(inEvent.input()), Paths.get(inEvent.output()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                LOGGER.info("Finished processing System.in");
            }
        }
    }

    private static void emitEvent(SMimeGenerationEventOut event) {
        try {
            System.out.println(objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write NDJSON event out", e);
        }
    }

    private record Processor(SMimeGenerator generator, Session session) {

        public void process(Path input, Path output) {
            try {
                MimeMessageInput mimeMessageInput = MimeMessageInput.fromJson(input);
                MimeMessage mimeMessage = mimeMessageInput.toMimeMessage(session);
                MimeMessage smimeMessage = generator.encrypt(generator.sign(mimeMessage));
                // Use inputUri file name sans extension
                //String outName = JPaths.fileNameWithoutExtension(inputJson);
                //Path outputFilePath = outputDir.resolve(outName);
                // Write to outputUri
                Files.writeString(output, MimeMessages.toString(smimeMessage), StandardCharsets.UTF_8);
                //logger.info("Generated S/MIME message for [{}] at: {}", inputJson, outputFilePath);
                emitEvent(SMimeGenerationEventOut.success(input, output));
            } catch (Exception e) {
                //logger.error("Failed to generate S/MIME message from JSON [{}]:", inputJson, e);
                emitEvent(SMimeGenerationEventOut.error(input, e));
            }
        }

    }
}