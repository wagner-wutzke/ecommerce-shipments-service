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
    var problem = handler.notFound(new ShipmentNotFoundException(UUID.randomUUID()));
    assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
  }

  @Test
  void mapsBadRequest() {
    var problem = handler.badRequest(new IllegalArgumentException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("bad", problem.getDetail());
  }
}
