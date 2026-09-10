package net.wowdev.ecommerce.shipments.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.OrderDTO;
import net.wowdev.ecommerce.domain.events.InvoiceCompleted;
import net.wowdev.ecommerce.shipments.service.ShipmentService;
import org.junit.jupiter.api.Test;

class ShipmentConsumerTest {
  @Test
  void acceptsUnknownEvents() {
    final ShipmentService shipmentService = mock(ShipmentService.class);
    assertDoesNotThrow(() -> new ShipmentConsumer(shipmentService).handleUnknown("unknown"));
  }

  @Test
  void processesInvoiceCompletedOrder() {
    ShipmentService shipmentService = mock(ShipmentService.class);
    ShipmentConsumer consumer = new ShipmentConsumer(shipmentService);
    OrderDTO order = mock(OrderDTO.class);
    InvoiceCompleted event =
        new InvoiceCompleted(UUID.randomUUID(), "transaction", order, Instant.now(), "invoices");

    consumer.handle(event);

    verify(shipmentService).process(order);
  }
}
