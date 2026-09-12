package com.amazingco.stocking.features.stock.repositories;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.ReservationStatus;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockReservationsRepositoryImplIT extends AbstractPostgresIntegrationTest {

    private static final OrderId ORDER_ID = OrderId.newId();
    private static final Sku SKU = new Sku("ABC-123");

    @Autowired
    private StockReservationsRepository stockReservationsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM stock_reservation");
    }

    @Test
    void saveThenFindByOrderIdAndSkuRoundTrips() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));

        stockReservationsRepository.save(reservation);

        StockReservation found = stockReservationsRepository.findByOrderIdAndSku(ORDER_ID, SKU).orElseThrow();
        assertEquals(reservation.id(), found.id());
        assertEquals(ORDER_ID, found.orderId());
        assertEquals(SKU, found.sku());
        assertEquals(Quantity.of(2), found.quantity());
        assertEquals(ReservationStatus.RESERVED, found.status());
    }

    @Test
    void saveUpsertsStatusTransitionWithoutDuplicating() {
        StockReservation reservation = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));
        stockReservationsRepository.save(reservation);

        reservation.confirm();
        stockReservationsRepository.save(reservation);

        StockReservation found = stockReservationsRepository.findByOrderIdAndSku(ORDER_ID, SKU).orElseThrow();
        assertEquals(ReservationStatus.CONFIRMED, found.status());
        Integer rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stock_reservation", Integer.class);
        assertEquals(1, rowCount);
    }

    @Test
    void findByOrderIdAndSkuReturnsEmptyWhenNoMatch() {
        assertTrue(stockReservationsRepository.findByOrderIdAndSku(ORDER_ID, SKU).isEmpty());
    }

    @Test
    void uniqueConstraintRejectsASecondDistinctReservationForTheSameOrderAndSku() {
        stockReservationsRepository.save(StockReservation.create(ORDER_ID, SKU, Quantity.of(2)));
        StockReservation duplicate = StockReservation.create(ORDER_ID, SKU, Quantity.of(2));

        // Different id (a genuinely new reservation attempt), same (orderId, sku) — this is
        // exactly the case the idempotency ledger's unique constraint exists to reject; the
        // use-case-level idempotency check (findByOrderIdAndSku before creating a new one) is
        // what's actually meant to prevent this in practice, but the DB constraint is the real
        // backstop per the saga architecture note.
        assertThrows(DataIntegrityViolationException.class,
                () -> stockReservationsRepository.save(duplicate));
    }
}
