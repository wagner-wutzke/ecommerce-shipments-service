package net.wowdev.ecommerce.shipments.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wowdev.ecommerce.domain.events.ShipmentCompletedEvent;
import net.wowdev.ecommerce.domain.events.ShipmentFailedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentProducer {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${app.kafka.shipments-topic}")
  private String topic;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
  public void publish(final ShipmentCompletedEvent event) {
    log.debug(">> Publishing ShipmentCompletedEvent: {}", event.eventId());
    kafkaTemplate.send(topic, event.eventId().toString(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
  public void publish(final ShipmentFailedEvent event) {
    log.debug(">> Publishing ShipmentFailedEvent: {}", event.eventId());
    kafkaTemplate.send(topic, event.eventId().toString(), event);
  }
}
