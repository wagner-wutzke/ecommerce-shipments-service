package net.wowdev.ecommerce.shipments.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.ShipmentDTO;
import net.wowdev.ecommerce.domain.enums.ShipmentStatus;
import net.wowdev.ecommerce.shipments.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

class ShipmentControllerTest {
  private final ShipmentService service = mock(ShipmentService.class);
  private final ShipmentController controller = new ShipmentController(service);

  private ShipmentDTO dto() {
    return new ShipmentDTO(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        ShipmentStatus.DELIVERED,
        "T",
        "C",
        "U",
        null,
        null);
  }

  @Test
  void delegatesCrud() {
    ShipmentDTO d = dto();
    UUID id = d.getId();
    when(service.findById(id)).thenReturn(d);
    when(service.create(d)).thenReturn(d);
    when(service.update(id, d)).thenReturn(d);
    assertSame(d, controller.get(id));
    ResponseEntity<ShipmentDTO> created = controller.create(d);
    assertEquals(201, created.getStatusCode().value());
    assertEquals(URI.create("/api/v1/shipments/" + id), created.getHeaders().getLocation());
    assertSame(d, created.getBody());
    assertSame(d, controller.update(id, d));
    assertEquals(204, controller.delete(id).getStatusCode().value());
    verify(service).delete(id);
  }

  @Test
  void listsWithDescendingCreatedAt() {
    when(service.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    assertEquals(0, controller.list(2, 10).getTotalElements());
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(service).findAll(pageable.capture());
    assertEquals(2, pageable.getValue().getPageNumber());
    assertEquals(10, pageable.getValue().getPageSize());
    assertEquals("createdAt", pageable.getValue().getSort().getOrderFor("createdAt").getProperty());
    assertTrue(pageable.getValue().getSort().getOrderFor("createdAt").isDescending());
  }
}
