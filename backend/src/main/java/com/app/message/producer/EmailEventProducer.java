package com.app.message.producer;

import com.app.Model.VerificationEmailEvent;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailEventProducer {

    private final KafkaTemplate<String, Object> emailKafkaTemplate;

    @Value("${kafka.topic.email-event.name}")
    private String emailEventTopicName;

    @Autowired
    public EmailEventProducer(KafkaTemplate<String, Object> emailKafkaTemplate) {
        this.emailKafkaTemplate = emailKafkaTemplate;
    }

    public void send(VerificationEmailEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event is empty");
        }

        log.info("Sending event to topic {}: {}", emailEventTopicName, event.toString());

        emailKafkaTemplate.send(emailEventTopicName, event);
    }
}
