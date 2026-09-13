package com.amazingco.core.event;

/**
 * Single source of truth for Kafka topic names, so services don't hardcode topic strings
 * independently. One topic per message type (not one shared result topic for both outcomes) so
 * each topic's Spring Kafka {@code JsonDeserializer} always resolves to a single concrete type.
 */
public final class Topics {

    public static final String RESERVE_STOCK_COMMANDS = "reserve-stock-commands";
    public static final String STOCK_RESERVED_EVENTS = "stock-reserved-events";
    public static final String STOCK_RESERVATION_FAILED_EVENTS = "stock-reservation-failed-events";

    private Topics() {
    }
}
