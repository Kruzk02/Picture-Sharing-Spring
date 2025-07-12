package com.app.Config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Value("${kafka.bootstrap-servers}")
  private String bootstrapAddress;

  @Value("${kafka.topic.email-event.name}")
  private String emailEventTopicName;

  @Value("${kafka.topic.email-event.partitions:1}")
  private int emailEventPartitions;

  @Value("${kafka.topic.email-event.replication-factor:1}")
  private short emailEventReplicationFactor;

  @Value("${kafka.topic.notification-event.name}")
  private String notificationEventTopicName;

  @Value("${kafka.topic.notification-event.partitions:1}")
  private int notificationEventPartitions;

  @Value("${kafka.topic.notification-event.replication-factor:1}")
  private short notificationEventReplicationFactor;

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic emailEventTopic() {
    return new NewTopic(emailEventTopicName, emailEventPartitions, emailEventReplicationFactor);
  }

  @Bean
  public NewTopic notificationEventTopic() {
    return new NewTopic(
        notificationEventTopicName,
        notificationEventPartitions,
        notificationEventReplicationFactor);
  }
}
