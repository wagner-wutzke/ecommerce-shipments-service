package net.wowdev.ecommerce.shipments.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

class ShipmentConsumerTest {
    @Test void acceptsUnknownEvents() { assertDoesNotThrow(() -> new ShipmentConsumer().handleUnknown("unknown")); }
}
