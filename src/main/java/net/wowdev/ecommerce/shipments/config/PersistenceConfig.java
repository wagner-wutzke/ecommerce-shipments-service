package net.wowdev.ecommerce.shipments.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = "net.wowdev.ecommerce.domain.entity")
public class PersistenceConfig {}
