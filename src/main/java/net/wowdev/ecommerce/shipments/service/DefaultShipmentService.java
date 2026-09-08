package net.wowdev.ecommerce.shipments.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wowdev.ecommerce.domain.dto.OrderDTO;
import net.wowdev.ecommerce.domain.dto.ShipmentDTO;
import net.wowdev.ecommerce.domain.entity.ShipmentEntity;
import net.wowdev.ecommerce.domain.enums.ShipmentStatus;
import net.wowdev.ecommerce.domain.events.ShipmentFailedEvent;
import net.wowdev.ecommerce.domain.mapper.ShipmentMapper;
import net.wowdev.ecommerce.shipments.messaging.ShipmentProducer;
import net.wowdev.ecommerce.shipments.repository.ShipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultShipmentService implements ShipmentService {

  private static final String ORIGIN_SERVICE = "SHIPMENTS-SERVICE";
  private final ShipmentRepository repository;
  private final ShipmentProducer shipmentProducer;

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
      if (processFails()) {
        throw new RuntimeException("Shipping Bill of Materials could not be generated.");
      }
      ShipmentDTO shipmentDTO = new ShipmentDTO(
          null,
          orderDTO.getId(),
          orderDTO.getCustomerId(),
          ShipmentStatus.REQUESTED,
          "tracking_number",
          "carrier",
          "tracking_url",
          null,
          null
      );
      repository.save(ShipmentMapper.toEntity(shipmentDTO));

    } catch (Exception e) {
      shipmentProducer.publish(
          new ShipmentFailedEvent(
              UUID.randomUUID(),
              orderDTO.getId().toString(),
              orderDTO,
              "Error placing the Shipment Request: " + e.getMessage(),
              Instant.now(),
              ORIGIN_SERVICE
          )
      );
    }
  }

  private boolean processFails() {
    int second = Instant.now().atZone(ZoneId.systemDefault()).getSecond();
    boolean failed = second % 5 == 0;
    log.debug(
        ">> Runtime condition for simulating process failure: [{} % 5 == 0 => {}]", second, failed);
    return failed;
  }
}
