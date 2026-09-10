package net.wowdev.ecommerce.shipments.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import net.wowdev.ecommerce.shipments.service.ShipmentNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionHandlerTest {
  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void mapsNotFound() {
    UUID id = UUID.randomUUID();
    var problem = handler.notFound(new ShipmentNotFoundException(id));
    assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
    assertEquals("Shipment record not found: " + id, problem.getDetail());
  }

  @Test
  void mapsBadRequest() {
    var problem = handler.badRequest(new IllegalArgumentException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("bad", problem.getDetail());
  }

  @Test
  void mapsValidationFailureToBadRequest() {
    var problem = handler.badRequest(new IllegalArgumentException("invalid shipment"));

    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("invalid shipment", problem.getDetail());
  }
}
