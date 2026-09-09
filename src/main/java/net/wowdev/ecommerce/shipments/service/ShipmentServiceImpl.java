package net.wowdev.ecommerce.shipments.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wowdev.ecommerce.domain.dto.OrderDTO;
import net.wowdev.ecommerce.domain.dto.ShipmentDTO;
import net.wowdev.ecommerce.domain.entity.ShipmentEntity;
import net.wowdev.ecommerce.domain.enums.ShipmentStatus;
import net.wowdev.ecommerce.domain.events.ShipmentCompletedEvent;
import net.wowdev.ecommerce.domain.events.ShipmentFailedEvent;
import net.wowdev.ecommerce.domain.mapper.ShipmentMapper;
import net.wowdev.ecommerce.shipments.messaging.ShipmentProducer;
import net.wowdev.ecommerce.shipments.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

  private static final String ORIGIN_SERVICE = "SHIPMENTS-SERVICE";
  private final ShipmentRepository repository;
  private final ShipmentProducer shipmentProducer;

  @Value(value = "${app.service.shipments.failing}")
  private boolean serviceIsFailing;

  @Override
  @Transactional(readOnly = true)
  public ShipmentDTO findById(final UUID id) {
    return repository
        .findById(id)
        .map(ShipmentMapper::toDto)
        .orElseThrow(() -> new ShipmentNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ShipmentDTO> findAll(final Pageable pageable) {
    return repository.findAll(pageable).map(ShipmentMapper::toDto);
  }

  @Override
  @Transactional
  public ShipmentDTO create(final ShipmentDTO shipment) {
    final ShipmentEntity entity = ShipmentMapper.toEntity(shipment);
    return ShipmentMapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public ShipmentDTO update(final UUID id, final ShipmentDTO shipment) {
    final ShipmentEntity current =
        repository.findById(id).orElseThrow(() -> new ShipmentNotFoundException(id));
    final ShipmentDTO replacement = ShipmentMapper.toDto(current);
    replacement.setOrderId(shipment.getOrderId());
    replacement.setCustomerId(shipment.getCustomerId());
    replacement.setShippingStatus(shipment.getShippingStatus());
    replacement.setTrackingNumber(shipment.getTrackingNumber());
    replacement.setCarrier(shipment.getCarrier());
    replacement.setTrackingUrl(shipment.getTrackingUrl());
    return ShipmentMapper.toDto(repository.save(ShipmentMapper.toEntity(replacement)));
  }

  @Override
  @Transactional
  public void delete(final UUID id) {
    if (!repository.existsById(id)) throw new ShipmentNotFoundException(id);
    repository.deleteById(id);
  }

  @Override
  public void process(OrderDTO orderDTO) {
    log.debug(">> Processing Shipment Request for order: {}", orderDTO.getId());
    log.debug(">> Shipment Request logic still need to be implemented...");

    try {
      if (serviceIsFailing()) {
        throw new RuntimeException("Shipment Bill of Materials could not be generated.");
      }
      ShipmentDTO shipmentDTO =
          new ShipmentDTO(
              null,
              orderDTO.getId(),
              orderDTO.getCustomerId(),
              ShipmentStatus.REQUESTED,
              "tracking_number",
              "carrier",
              "tracking_url",
              null,
              null);
      ShipmentDTO saved =
          ShipmentMapper.toDto(repository.save(ShipmentMapper.toEntity(shipmentDTO)));
      log.debug(">> Shipment Request processed successfully...");
      publishShipmentCompletedEvent(orderDTO, saved);
    } catch (Exception e) {
      publishShipmentFailedEvent(orderDTO, e);
    }
  }

  private void publishShipmentFailedEvent(OrderDTO orderDTO, Exception e) {
    shipmentProducer.publish(
        new ShipmentFailedEvent(
            UUID.randomUUID(),
            orderDTO.getId().toString(),
            orderDTO,
            "Error processing the Shipment Request: " + e.getMessage(),
            Instant.now(),
            ORIGIN_SERVICE));
  }

  private void publishShipmentCompletedEvent(OrderDTO orderDTO, ShipmentDTO shipmentDTO) {
    shipmentProducer.publish(
        new ShipmentCompletedEvent(
            UUID.randomUUID(),
            orderDTO.getId().toString(),
            orderDTO,
            shipmentDTO,
            Instant.now(),
            ORIGIN_SERVICE));
  }

  private boolean serviceIsFailing() {
    if (this.serviceIsFailing) {
      log.debug(
          """
          >> Service is configured o be failing when processing events. "
             See "app.service.shipments.failing" or "SERVICE_SHIPMENTS_FAILING" environment var.
          """);
    }
    return this.serviceIsFailing;
  }
}
