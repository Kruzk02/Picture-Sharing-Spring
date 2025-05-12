package com.app.message.producer;

import com.app.Model.Notification;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class NotificationEventProducer {

    private final KafkaTemplate<String, Object> notificationKafkaTemplate;
    @Value("${kafka.topic.notification-event.name}")
    private String notificationEventTopicName;

    @Autowired
    public NotificationEventProducer(KafkaTemplate<String, Object> notificationKafkaTemplate) {
        this.notificationKafkaTemplate = notificationKafkaTemplate;
    }

    public void send(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification is empty");
        }

        log.info("Sending event to topic {} : {}", notificationEventTopicName, notification.toString());

        notificationKafkaTemplate.send(notificationEventTopicName, notification);
    }
}
