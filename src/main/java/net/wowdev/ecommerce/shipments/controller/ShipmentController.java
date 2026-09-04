package net.wowdev.ecommerce.shipments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.wowdev.ecommerce.domain.dto.ShippingDTO;
import net.wowdev.ecommerce.shipments.service.ShipmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

  private final ShipmentService service;

  @GetMapping("/{id}")
  public ShippingDTO get(@PathVariable final UUID id) {
    return service.findById(id);
  }

  @GetMapping
  public Page<ShippingDTO> list(
      @RequestParam(defaultValue = "0") @Min(0) final int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int pageSize) {
    return service.findAll(
        PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  @PostMapping
  public ResponseEntity<ShippingDTO> create(@Valid @RequestBody final ShippingDTO shipment) {
    final ShippingDTO created = service.create(shipment);
    return ResponseEntity.created(URI.create("/api/v1/shipments/" + created.getId())).body(created);
  }

  @PutMapping("/{id}")
  public ShippingDTO update(
      @PathVariable final UUID id, @Valid @RequestBody final ShippingDTO shipment) {
    return service.update(id, shipment);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable final UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
