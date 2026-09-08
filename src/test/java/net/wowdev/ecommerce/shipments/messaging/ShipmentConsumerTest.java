package net.wowdev.ecommerce.shipments.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import net.wowdev.ecommerce.shipments.service.ShipmentService;
import org.junit.jupiter.api.Test;

class ShipmentConsumerTest {
  @Test
  void acceptsUnknownEvents() {
    final ShipmentService shipmentService = mock(ShipmentService.class);
    assertDoesNotThrow(() -> new ShipmentConsumer(shipmentService).handleUnknown("unknown"));
  }
}
