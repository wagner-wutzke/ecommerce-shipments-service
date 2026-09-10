package net.wowdev.ecommerce.shipments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShipmentNotFoundExceptionTest {
  @Test
  void describesMissingShipmentId() {
    UUID id = UUID.randomUUID();

    assertEquals(
        "Shipment record not found: " + id, new ShipmentNotFoundException(id).getMessage());
  }
}
