package devkn.jtools.smimegen.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record SMimeGenerationRequest(
        String signingX509Uri,
        String signingPkcs8Uri,
        String signingPkcs8Password,
        String encryptX509Uri,
        String mimeMessagesDirUri,
        String outputDirUri
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static SMimeGenerationRequest fromJsonString(String arg) {
        try {
            return mapper.readValue(arg, SMimeGenerationRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
