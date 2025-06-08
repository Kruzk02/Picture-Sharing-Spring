package com.app.Config;

import com.app.serde.serializer.NotificationEventSerializer;
import com.app.serde.serializer.VerificationEmailEventSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.CompositeProducerListener;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapAddress;

    private ProducerFactory<String, Object> createProductFactory(Class<?> clazz) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, clazz);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    private KafkaTemplate<String, Object> createKafkaTemplate(ProducerFactory<String, Object> factory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(factory);
        template.setProducerListener(new CompositeProducerListener<>());
        return template;
    }

    @Bean
    public KafkaTemplate<String, Object> emailKafkaTemplate() {
        return createKafkaTemplate(createProductFactory(VerificationEmailEventSerializer.class));
    }

    @Bean
    public KafkaTemplate<String, Object> notificationKafkaTemplate() {
        return createKafkaTemplate(createProductFactory(NotificationEventSerializer.class));
    }

}
