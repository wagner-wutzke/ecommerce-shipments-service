package net.wowdev.ecommerce.shipments.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wowdev.ecommerce.domain.events.PaymentCompletedEvent;
import net.wowdev.ecommerce.shipments.service.ShipmentService;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    groupId = "${spring.kafka.consumer.group-id}",
    topics = {"${app.kafka.payments-topic}", "${app.kafka.shipments-topic}"},
    containerFactory = "kafkaListenerContainerFactory")
public class ShipmentConsumer {

  private final ShipmentService shipmentService;

  @KafkaHandler(isDefault = true)
  public void handleUnknown(final Object event) {
    log.debug(">> Received an unmapped event of type {}", event.getClass().getSimpleName());
  }

  @KafkaHandler
  public void handlePaymentCompleted(final PaymentCompletedEvent event) {
    log.debug(
        ">> Processing PaymentCompletedEvent event sent from {}. Event id {}",
        event.origin(),
        event.eventId());
    shipmentService.process(event.orderDTO());
  }
}
