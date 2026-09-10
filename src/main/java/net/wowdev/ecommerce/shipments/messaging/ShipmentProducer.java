package net.wowdev.ecommerce.shipments.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wowdev.ecommerce.domain.events.ShipmentCompleted;
import net.wowdev.ecommerce.domain.events.ShipmentFailed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentProducer {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${app.kafka.shipments-topic}")
  private String topic;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
  public void publish(final ShipmentCompleted event) {
    log.debug(">> Publishing ShipmentCompleted event: {}", event.eventId());
    kafkaTemplate.send(topic, event.eventId().toString(), event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
  public void publish(final ShipmentFailed event) {
    log.debug(">> Publishing ShipmentFailed event: {}", event.eventId());
    kafkaTemplate.send(topic, event.eventId().toString(), event);
  }
}
