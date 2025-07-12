package com.app.serde.serializer;

import com.app.Model.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Serializer;

public class NotificationEventSerializer implements Serializer<Notification> {

  private final ObjectMapper objectMapper;

  public NotificationEventSerializer() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @SneakyThrows
  @Override
  public byte[] serialize(String s, Notification notification) {
    return objectMapper.writeValueAsBytes(notification);
  }
}
