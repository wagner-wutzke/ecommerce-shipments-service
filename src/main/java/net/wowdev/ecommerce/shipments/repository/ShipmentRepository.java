package net.wowdev.ecommerce.shipments.repository;

import java.util.UUID;
import net.wowdev.ecommerce.domain.entity.ShippingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<ShippingEntity, UUID> {}
