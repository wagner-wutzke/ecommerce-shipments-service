package net.wowdev.ecommerce.shipments.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.wowdev.ecommerce.domain.dto.ShippingDTO;
import net.wowdev.ecommerce.domain.entity.ShippingEntity;
import net.wowdev.ecommerce.domain.events.ShippingCompletedEvent;
import net.wowdev.ecommerce.domain.mapper.ShippingMapper;
import net.wowdev.ecommerce.shipments.messaging.ShipmentProducer;
import net.wowdev.ecommerce.shipments.repository.ShipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultShipmentService implements ShipmentService {
  private static final String ORIGIN = "shipments-service";
  private final ShipmentRepository repository;
  private final ShipmentProducer shipmentProducer;

  @Override
  @Transactional(readOnly = true)
  public ShippingDTO findById(final UUID id) {
    return repository
        .findById(id)
        .map(ShippingMapper::toDto)
        .orElseThrow(() -> new ShipmentNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ShippingDTO> findAll(final Pageable pageable) {
    return repository.findAll(pageable).map(ShippingMapper::toDto);
  }

  @Override
  @Transactional
  public ShippingDTO create(final ShippingDTO shipment) {
    final ShippingEntity entity = ShippingMapper.toEntity(shipment);
    final ShippingDTO saved = ShippingMapper.toDto(repository.save(entity));
    shipmentProducer.publishAfterCommit(
        new ShippingCompletedEvent(
            UUID.randomUUID(), saved.getId().toString(), null, saved, Instant.now(), ORIGIN));
    return saved;
  }

  @Override
  @Transactional
  public ShippingDTO update(final UUID id, final ShippingDTO shipment) {
    final ShippingEntity current =
        repository.findById(id).orElseThrow(() -> new ShipmentNotFoundException(id));
    final ShippingDTO replacement = ShippingMapper.toDto(current);
    replacement.setOrderId(shipment.getOrderId());
    replacement.setCustomerId(shipment.getCustomerId());
    replacement.setShippingStatus(shipment.getShippingStatus());
    replacement.setTrackingNumber(shipment.getTrackingNumber());
    replacement.setCarrier(shipment.getCarrier());
    replacement.setTrackingUrl(shipment.getTrackingUrl());
    final ShippingDTO saved =
        ShippingMapper.toDto(repository.save(ShippingMapper.toEntity(replacement)));
    shipmentProducer.publishAfterCommit(
        new ShippingCompletedEvent(
            UUID.randomUUID(), id.toString(), null, saved, Instant.now(), ORIGIN));
    return saved;
  }

  @Override
  @Transactional
  public void delete(final UUID id) {
    if (!repository.existsById(id)) throw new ShipmentNotFoundException(id);
    repository.deleteById(id);
  }
}
