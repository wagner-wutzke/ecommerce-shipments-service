package net.wowdev.ecommerce.shipments.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.ShipmentDTO;
import net.wowdev.ecommerce.domain.entity.ShipmentEntity;
import net.wowdev.ecommerce.domain.enums.ShipmentStatus;
import net.wowdev.ecommerce.shipments.messaging.ShipmentProducer;
import net.wowdev.ecommerce.shipments.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {
  @Mock ShipmentRepository repository;
  @Mock ShipmentProducer shipmentProducer;
  @InjectMocks
  ShipmentServiceImpl service;

  private ShipmentDTO dto(final UUID id) {
    return new ShipmentDTO(
        id,
        UUID.randomUUID(),
        UUID.randomUUID(),
        ShipmentStatus.IN_TRANSIT,
        "TRK-1",
        "Carrier",
        "https://track",
        null,
        null);
  }

  private ShipmentEntity entity(final UUID id) {
    return new ShipmentEntity(
        id,
        UUID.randomUUID(),
        UUID.randomUUID(),
        ShipmentStatus.IN_TRANSIT,
        "TRK-1",
        "Carrier",
        "https://track",
        null,
        null);
  }

  @Test
  void findByIdMapsAndThrows() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.of(entity(id)));
    assertEquals(id, service.findById(id).getId());
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThrows(ShipmentNotFoundException.class, () -> service.findById(id));
  }

  @Test
  void findAllMapsPage() {
    when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity(UUID.randomUUID()))));
    assertEquals(1, service.findAll(PageRequest.of(0, 2)).getTotalElements());
  }

  @Test
  void createSuccessfully() {
    UUID id = UUID.randomUUID();
    when(repository.save(any())).thenReturn(entity(id));
    assertEquals(id, service.create(dto(id)).getId());
  }

  @Test
  void updateCopiesMutableFields() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.of(entity(id)));
    when(repository.save(any())).thenReturn(entity(id));
    ShipmentDTO result = service.update(id, dto(id));
    assertEquals(id, result.getId());
  }

  @Test
  void updateMissingThrows() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThrows(ShipmentNotFoundException.class, () -> service.update(id, dto(id)));
  }

  @Test
  void deleteChecksExistence() {
    UUID id = UUID.randomUUID();
    when(repository.existsById(id)).thenReturn(true);
    service.delete(id);
    verify(repository).deleteById(id);
    when(repository.existsById(id)).thenReturn(false);
    assertThrows(ShipmentNotFoundException.class, () -> service.delete(id));
  }
}
