package net.wowdev.ecommerce.shipments.messaging;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.ShippingDTO;
import net.wowdev.ecommerce.domain.enums.DeliveryStatus;
import net.wowdev.ecommerce.domain.events.ShipmentCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class ShipmentProducerTest {
  @Test
  void publishesUsingStableShippingKey() {
    KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
    ShipmentProducer producer = new ShipmentProducer(template);
    ReflectionTestUtils.setField(producer, "topic", "shipments-topic");
    UUID id = UUID.randomUUID();
    ShippingDTO dto =
        new ShippingDTO(id, null, null, DeliveryStatus.IN_TRANSIT, "T", "C", "U", null, null);
    ShipmentCompletedEvent event =
        new ShipmentCompletedEvent(UUID.randomUUID(), "tx", null, dto, Instant.now(), "test");
    producer.publishAfterCommit(event);
    verify(template).send("shipments-topic", id.toString(), event);
  }
}
