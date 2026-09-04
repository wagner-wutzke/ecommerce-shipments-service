package net.wowdev.ecommerce.shipments.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaConfigTest {
    private KafkaConfig config;
    @BeforeEach void setUp() { config = new KafkaConfig(); ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092"); ReflectionTestUtils.setField(config, "consumerGroup", "test"); ReflectionTestUtils.setField(config, "trustedPackages", "net.wowdev.ecommerce"); ReflectionTestUtils.setField(config, "retries", 3); ReflectionTestUtils.setField(config, "acks", "all"); ReflectionTestUtils.setField(config, "deliveryTimeout", "30000"); ReflectionTestUtils.setField(config, "requestTimeout", "10000"); ReflectionTestUtils.setField(config, "linger", "0"); }
    @Test void createsFactories() { assertNotNull(config.producerFactory()); assertNotNull(config.consumerFactory()); KafkaTemplate<String,Object> template = config.kafkaTemplate(config.producerFactory()); assertNotNull(template); assertNotNull(config.kafkaListenerContainerFactory(config.consumerFactory(), template)); }
}
