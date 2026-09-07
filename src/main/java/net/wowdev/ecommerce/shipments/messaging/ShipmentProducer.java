package net.wowdev.ecommerce.shipments.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wowdev.ecommerce.domain.events.ShipmentCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentProducer {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${app.kafka.shipments-topic}")
  private String topic;

  public void publishAfterCommit(final ShipmentCompletedEvent event) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              send(event);
            }
          });
      return;
    }
    send(event);
  }

  private void send(final ShipmentCompletedEvent event) {
    final String key = event.shippingDTO().getId().toString();
    log.debug(">> Publishing ShipmentCompletedEvent {} with key {}", event.eventId(), key);
    kafkaTemplate.send(topic, key, event);
  }
}
