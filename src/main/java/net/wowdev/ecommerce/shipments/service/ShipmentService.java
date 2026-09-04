package net.wowdev.ecommerce.shipments.service;

import java.util.UUID;
import net.wowdev.ecommerce.domain.dto.ShippingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShipmentService {
  ShippingDTO findById(UUID id);

  Page<ShippingDTO> findAll(Pageable pageable);

  ShippingDTO create(ShippingDTO shipment);

  ShippingDTO update(UUID id, ShippingDTO shipment);

  void delete(UUID id);
}
