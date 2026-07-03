package devkn.jtools.smimegen.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

public record SMimeGenerationEventIn(URI input, URI output) {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SMimeGenerationEventIn of(String json) {
        try {
            InEventData inEventData = objectMapper.readValue(json, InEventData.class);
            return inEventData.toEvent();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private record InEventData(
            String inputUri,
            String outputUri
    ) {
        public SMimeGenerationEventIn toEvent() {
            return new SMimeGenerationEventIn(URI.create(inputUri), URI.create(outputUri));
        }
    }
}
