package net.wowdev.ecommerce.shipments.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.OrderDTO;
import net.wowdev.ecommerce.domain.dto.ShipmentDTO;
import net.wowdev.ecommerce.domain.entity.ShipmentEntity;
import net.wowdev.ecommerce.domain.enums.ShipmentStatus;
import net.wowdev.ecommerce.domain.events.ShipmentCompleted;
import net.wowdev.ecommerce.domain.events.ShipmentFailed;
import net.wowdev.ecommerce.shipments.messaging.ShipmentProducer;
import net.wowdev.ecommerce.shipments.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {
  @Mock ShipmentRepository repository;
  @Mock ShipmentProducer shipmentProducer;
  @InjectMocks ShipmentServiceImpl service;

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
    ShipmentEntity saved = verifyAndCaptureSavedEntity();
    assertEquals(ShipmentStatus.IN_TRANSIT, saved.getShippingStatus());
    assertEquals("TRK-1", saved.getTrackingNumber());
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

  @Test
  void processSavesShipmentAndPublishesCompletedEvent() {
    UUID orderId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    OrderDTO order = mock(OrderDTO.class);
    when(order.getId()).thenReturn(orderId);
    when(order.getCustomerId()).thenReturn(customerId);
    when(repository.save(any(ShipmentEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    ReflectionTestUtils.setField(service, "failsWhenRunning", false);

    service.process(order);

    ArgumentCaptor<ShipmentCompleted> event = ArgumentCaptor.forClass(ShipmentCompleted.class);
    verify(shipmentProducer).publish(event.capture());
    assertEquals(orderId.toString(), event.getValue().transactionId());
    assertSame(order, event.getValue().orderDTO());
    assertEquals(ShipmentStatus.REQUESTED, event.getValue().shipmentDTO().getShippingStatus());
    assertEquals("SHIPMENTS-SERVICE", event.getValue().origin());
    verify(repository).save(any(ShipmentEntity.class));
    verifyNoMoreInteractions(shipmentProducer);
  }

  @Test
  void processPublishesFailureWhenConfiguredToFail() {
    UUID orderId = UUID.randomUUID();
    OrderDTO order = mock(OrderDTO.class);
    when(order.getId()).thenReturn(orderId);
    ReflectionTestUtils.setField(service, "failsWhenRunning", true);

    service.process(order);

    ArgumentCaptor<ShipmentFailed> event = ArgumentCaptor.forClass(ShipmentFailed.class);
    verify(shipmentProducer).publish(event.capture());
    assertEquals("Shipment Bill of Materials is missing.", event.getValue().reason());
    assertSame(order, event.getValue().orderDTO());
    verify(repository, never()).save(any(ShipmentEntity.class));
  }

  @Test
  void processPublishesFailureWhenSavingFails() {
    UUID orderId = UUID.randomUUID();
    OrderDTO order = mock(OrderDTO.class);
    when(order.getId()).thenReturn(orderId);
    when(order.getCustomerId()).thenReturn(UUID.randomUUID());
    when(repository.save(any(ShipmentEntity.class)))
        .thenThrow(new IllegalStateException("database down"));
    ReflectionTestUtils.setField(service, "failsWhenRunning", false);

    service.process(order);

    ArgumentCaptor<ShipmentFailed> event = ArgumentCaptor.forClass(ShipmentFailed.class);
    verify(shipmentProducer).publish(event.capture());
    assertEquals("database down", event.getValue().reason());
  }

  private ShipmentEntity verifyAndCaptureSavedEntity() {
    ArgumentCaptor<ShipmentEntity> entity = ArgumentCaptor.forClass(ShipmentEntity.class);
    verify(repository).save(entity.capture());
    return entity.getValue();
  }
}
