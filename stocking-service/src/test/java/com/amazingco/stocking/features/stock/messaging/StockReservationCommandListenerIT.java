package com.amazingco.stocking.features.stock.messaging;

import com.amazingco.core.event.ReserveStockForOrderCommand;
import com.amazingco.core.event.StockReservationFailedEvent;
import com.amazingco.core.event.StockReservationLineItem;
import com.amazingco.core.event.StockReservedEvent;
import com.amazingco.core.event.Topics;
import com.amazingco.stocking.AbstractPostgresIntegrationTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * The one genuine Testcontainers-backed Kafka IT per CLAUDE.md's testing conventions ("against
 * real Postgres/Kafka rather than mocks"). Publishes a real {@link ReserveStockForOrderCommand}
 * and lets the real {@link StockReservationCommandListener} bean (part of this
 * {@code @SpringBootTest} context) consume it, observing the real outcome topic with a raw
 * consumer rather than adding a second {@code @KafkaListener} bean just for observation.
 */
class StockReservationCommandListenerIT extends AbstractPostgresIntegrationTest {

    static final KafkaContainer KAFKA;

    static {
        KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));
        KAFKA.start();
    }

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Consumer<String, Object> consumer;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM stock_reservation");
        jdbcTemplate.update("DELETE FROM stock");

        Map<String, Object> props = KafkaTestUtils.consumerProps(KAFKA.getBootstrapServers(),
                "test-consumer-" + UUID.randomUUID(), "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.amazingco.core.event");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(Topics.STOCK_RESERVED_EVENTS, Topics.STOCK_RESERVATION_FAILED_EVENTS));
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    private void seedStock(String sku, int available) {
        jdbcTemplate.update("INSERT INTO stock (sku, available, reserved) VALUES (?, ?, 0)", sku, available);
    }

    @Test
    void reservesAllLinesAndPublishesSuccessEvent() {
        seedStock("ABC-123", 10);
        seedStock("XYZ-789", 10);
        UUID orderId = UUID.randomUUID();
        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand("trace-1", orderId, List.of(
                new StockReservationLineItem("ABC-123", 2, new BigDecimal("9.99"), "USD"),
                new StockReservationLineItem("XYZ-789", 1, new BigDecimal("5.00"), "USD")));

        kafkaTemplate.send(Topics.RESERVE_STOCK_COMMANDS, orderId.toString(), command);

        ConsumerRecord<String, Object> record = KafkaTestUtils.getSingleRecord(consumer, Topics.STOCK_RESERVED_EVENTS,
                Duration.ofSeconds(15));
        assertEquals(orderId.toString(), record.key());
        StockReservedEvent event = assertInstanceOf(StockReservedEvent.class, record.value());
        assertEquals(orderId, event.orderId());

        assertEquals(8, availableFor("ABC-123"));
        assertEquals(2, reservedFor("ABC-123"));
        assertEquals(9, availableFor("XYZ-789"));
        assertEquals(1, reservedFor("XYZ-789"));
    }

    @Test
    void insufficientStockOnSecondLineReleasesTheFirstAndPublishesFailureEvent() {
        seedStock("ABC-123", 10);
        seedStock("XYZ-789", 1);
        UUID orderId = UUID.randomUUID();
        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand("trace-1", orderId, List.of(
                new StockReservationLineItem("ABC-123", 2, new BigDecimal("9.99"), "USD"),
                new StockReservationLineItem("XYZ-789", 100, new BigDecimal("5.00"), "USD")));

        kafkaTemplate.send(Topics.RESERVE_STOCK_COMMANDS, orderId.toString(), command);

        ConsumerRecord<String, Object> record = KafkaTestUtils.getSingleRecord(consumer,
                Topics.STOCK_RESERVATION_FAILED_EVENTS, Duration.ofSeconds(15));
        assertEquals(orderId.toString(), record.key());
        StockReservationFailedEvent event = assertInstanceOf(StockReservationFailedEvent.class, record.value());
        assertEquals(orderId, event.orderId());

        // The first line's reservation must have been released back.
        assertEquals(10, availableFor("ABC-123"));
        assertEquals(0, reservedFor("ABC-123"));
        assertEquals(1, availableFor("XYZ-789"));
        assertEquals(0, reservedFor("XYZ-789"));
    }

    private Integer availableFor(String sku) {
        return jdbcTemplate.queryForObject("SELECT available FROM stock WHERE sku = ?", Integer.class, sku);
    }

    private Integer reservedFor(String sku) {
        return jdbcTemplate.queryForObject("SELECT reserved FROM stock WHERE sku = ?", Integer.class, sku);
    }
}
