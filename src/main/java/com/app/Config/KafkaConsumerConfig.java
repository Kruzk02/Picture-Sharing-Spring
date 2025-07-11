package com.app.Config;

import com.app.serde.deserializer.NotificationEventDeserializer;
import com.app.serde.deserializer.VerificationEmailEventDeserializer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConsumerConfig {

  @Value("${kafka.bootstrap-servers}")
  private String bootstrapAddress;

  private ConsumerFactory<String, Object> createConsumerFactory(String groupId, Class<?> clazz) {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, clazz);
    return new DefaultKafkaConsumerFactory<>(configs);
  }

  private ConcurrentKafkaListenerContainerFactory<String, Object> createListenerContainerFactory(
      ConsumerFactory<String, Object> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setConcurrency(3);
    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object>
      emailKafkaListenerContainerFactory() {
    return createListenerContainerFactory(
        createConsumerFactory("email-group", VerificationEmailEventDeserializer.class));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object>
      notificationKafkaListenerContainerFactory() {
    return createListenerContainerFactory(
        createConsumerFactory("notification-group", NotificationEventDeserializer.class));
  }
}
