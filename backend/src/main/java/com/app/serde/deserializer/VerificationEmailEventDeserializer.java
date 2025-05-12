package com.app.serde.deserializer;

import com.app.Model.VerificationEmailEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;

public class VerificationEmailEventDeserializer implements Deserializer<VerificationEmailEvent> {

    private final ObjectMapper objectMapper;

    public VerificationEmailEventDeserializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    @Override
    public VerificationEmailEvent deserialize(String s, byte[] bytes) {
        return objectMapper.readValue(bytes, VerificationEmailEvent.class);
    }
}
