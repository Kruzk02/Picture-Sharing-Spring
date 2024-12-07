package com.app.serde.serializer;

import com.app.Model.VerificationEmailEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Serializer;

public class VerificationEmailEventSerializer implements Serializer<VerificationEmailEvent> {

    private final ObjectMapper objectMapper;

    public VerificationEmailEventSerializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    @Override
    public byte[] serialize(String s, VerificationEmailEvent event) {
        return objectMapper.writeValueAsBytes(event);
    }
}
