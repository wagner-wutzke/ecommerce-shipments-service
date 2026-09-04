package net.wowdev.ecommerce.shipments.service;

import java.util.UUID;

public class ShipmentNotFoundException extends RuntimeException {
  public ShipmentNotFoundException(final UUID id) {
    super("Shipment record not found: " + id);
  }
}
