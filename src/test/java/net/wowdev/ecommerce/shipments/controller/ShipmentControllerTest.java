package net.wowdev.ecommerce.shipments.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.ShippingDTO;
import net.wowdev.ecommerce.domain.enums.DeliveryStatus;
import net.wowdev.ecommerce.shipments.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

class ShipmentControllerTest {
    private final ShipmentService service = mock(ShipmentService.class);
    private final ShipmentController controller = new ShipmentController(service);
    private ShippingDTO dto() { return new ShippingDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), DeliveryStatus.DELIVERED, "T", "C", "U", null, null); }
    @Test void delegatesCrud() { ShippingDTO d = dto(); UUID id = d.getId(); when(service.findById(id)).thenReturn(d); when(service.create(d)).thenReturn(d); when(service.update(id, d)).thenReturn(d); assertSame(d, controller.get(id)); assertEquals(201, controller.create(d).getStatusCode().value()); assertSame(d, controller.update(id, d)); assertEquals(204, controller.delete(id).getStatusCode().value()); verify(service).delete(id); }
    @Test void listsWithDescendingCreatedAt() { when(service.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of())); assertEquals(0, controller.list(2, 10).getTotalElements()); verify(service).findAll(any(Pageable.class)); }
}
