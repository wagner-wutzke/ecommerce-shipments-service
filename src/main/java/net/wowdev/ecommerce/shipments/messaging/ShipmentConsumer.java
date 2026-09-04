package net.wowdev.ecommerce.shipments.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    groupId = "${spring.kafka.consumer.group-id}",
    topics = {"${app.kafka.shipments-topic}"},
    containerFactory = "kafkaListenerContainerFactory")
public class ShipmentConsumer {

  @KafkaHandler(isDefault = true)
  public void handleUnknown(final Object event) {
    log.debug(">> Received an unmapped event of type {}", event.getClass().getSimpleName());
  }
}
