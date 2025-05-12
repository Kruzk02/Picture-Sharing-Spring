package com.app.serde.deserializer;

import com.app.Model.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;

public class NotificationEventDeserializer implements Deserializer<Notification> {

    private final ObjectMapper objectMapper;

    public NotificationEventDeserializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    @Override
    public Notification deserialize(String s, byte[] bytes) {
        return objectMapper.readValue(bytes, Notification.class);
    }
}
