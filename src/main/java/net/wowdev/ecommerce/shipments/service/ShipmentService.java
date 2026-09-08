package net.wowdev.ecommerce.shipments.service;

import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.OrderDTO;
import net.wowdev.ecommerce.domain.dto.ShipmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShipmentService {
  ShipmentDTO findById(UUID id);

  Page<ShipmentDTO> findAll(Pageable pageable);

  ShipmentDTO create(ShipmentDTO shipment);

  ShipmentDTO update(UUID id, ShipmentDTO shipment);

  void delete(UUID id);

  void process(OrderDTO orderDTO);

}
