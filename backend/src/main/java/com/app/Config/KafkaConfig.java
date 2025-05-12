package com.app.Config;

import com.app.Model.Notification;
import com.app.serde.deserializer.NotificationEventDeserializer;
import com.app.serde.deserializer.VerificationEmailEventDeserializer;
import com.app.serde.serializer.NotificationEventSerializer;
import com.app.serde.serializer.VerificationEmailEventSerializer;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.CompositeProducerListener;

import java.util.HashMap;
import java.util.Map;

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

    private ProducerFactory<String, Object> emailProducerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VerificationEmailEventSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, Object> emailKafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(emailProducerFactory());
        template.setProducerListener(new CompositeProducerListener<>());
        return template;
    }


    private ConsumerFactory<String, Object> emailConsumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "email-group");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, VerificationEmailEventDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> emailKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailConsumerFactory());
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public NewTopic notificationEventTopic() {
        return new NewTopic(notificationEventTopicName, notificationEventPartitions, notificationEventReplicationFactor);
    }

    private ProducerFactory<String, Object> notificationProducerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NotificationEventSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, Object> notificationKafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(notificationProducerFactory());
        template.setProducerListener(new CompositeProducerListener<>());
        return template;
    }

    private ConsumerFactory<String, Notification> notificationConsumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, NotificationEventDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Notification> notificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Notification> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}
