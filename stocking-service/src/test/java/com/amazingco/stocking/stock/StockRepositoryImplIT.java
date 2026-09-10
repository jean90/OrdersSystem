package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link StockRepository} has no insert method by design — every write is an atomic
 * conditional {@code UPDATE} on a row that must already exist (see the concurrency
 * architecture note). So these tests seed the {@code stock} row directly via
 * {@link JdbcTemplate}, the way a not-yet-built "initialize stock" flow eventually would.
 */
class StockRepositoryImplIT extends AbstractPostgresIntegrationTest {

    private static final Sku SKU = new Sku("ABC-123");

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM stock");
    }

    private void seedStock(int available, int reserved) {
        jdbcTemplate.update("INSERT INTO stock (sku, available, reserved) VALUES (?, ?, ?)",
                SKU.value(), available, reserved);
    }

    @Test
    void tryReserveMovesQuantityFromAvailableToReservedWhenEnoughStock() {
        seedStock(10, 0);

        boolean reserved = stockRepository.tryReserve(SKU, Quantity.of(4));

        assertTrue(reserved);
        Stock stock = stockRepository.findBySku(SKU).orElseThrow();
        assertEquals(Quantity.of(6), stock.available());
        assertEquals(Quantity.of(4), stock.reserved());
    }

    @Test
    void tryReserveFailsAndLeavesRowUnchangedWhenInsufficientAvailable() {
        seedStock(3, 0);

        boolean reserved = stockRepository.tryReserve(SKU, Quantity.of(5));

        assertFalse(reserved);
        Stock stock = stockRepository.findBySku(SKU).orElseThrow();
        assertEquals(Quantity.of(3), stock.available());
        assertEquals(Quantity.ZERO, stock.reserved());
    }

    @Test
    void tryReserveFailsWhenSkuIsUnknown() {
        boolean reserved = stockRepository.tryReserve(SKU, Quantity.of(1));

        assertFalse(reserved);
        assertTrue(stockRepository.findBySku(SKU).isEmpty());
    }

    @Test
    void confirmReservationOnlyDecrementsReserved() {
        seedStock(6, 4);

        stockRepository.confirmReservation(SKU, Quantity.of(4));

        Stock stock = stockRepository.findBySku(SKU).orElseThrow();
        assertEquals(Quantity.of(6), stock.available());
        assertEquals(Quantity.ZERO, stock.reserved());
    }

    @Test
    void releaseReservationMovesQuantityBackToAvailable() {
        seedStock(6, 4);

        stockRepository.releaseReservation(SKU, Quantity.of(4));

        Stock stock = stockRepository.findBySku(SKU).orElseThrow();
        assertEquals(Quantity.of(10), stock.available());
        assertEquals(Quantity.ZERO, stock.reserved());
    }

    @Test
    void findBySkuReturnsEmptyWhenUnknown() {
        assertTrue(stockRepository.findBySku(SKU).isEmpty());
    }
}
