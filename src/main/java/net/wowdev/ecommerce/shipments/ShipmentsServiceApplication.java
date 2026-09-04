package net.wowdev.ecommerce.shipments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {"net.wowdev.ecommerce.shipments", "net.wowdev.ecommerce.domain.entity"})
public class ShipmentsServiceApplication {
  public static void main(final String[] args) {
    SpringApplication.run(ShipmentsServiceApplication.class, args);
  }
}
