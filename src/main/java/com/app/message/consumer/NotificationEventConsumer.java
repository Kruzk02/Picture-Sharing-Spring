package com.app.message.consumer;

import com.app.Model.Notification;
import com.app.Service.NotificationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class NotificationEventConsumer {

  private final NotificationService notificationService;

  @Autowired
  public NotificationEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @KafkaListener(
      topics = "${kafka.topic.notification-event.name}",
      groupId = "notification-group",
      containerFactory = "notificationKafkaListenerContainerFactory")
  public void listen(Notification notification) {
    log.info("Receive event from topic: {}", notification.toString());
    notificationService.save(notification);
  }
}
