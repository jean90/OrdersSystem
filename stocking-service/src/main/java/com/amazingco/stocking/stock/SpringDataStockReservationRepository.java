package com.amazingco.stocking.stock;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * The actual Spring Data JDBC adapter behind {@link StockReservationsRepositoryImpl}.
 * {@code save} is deliberately not used: {@code id} is an application-assigned UUID
 * (generated once by {@code StockReservation.create}), so — same reasoning as
 * {@code SpringDataProductRepository} — an explicit upsert sidesteps Spring Data JDBC's
 * insert-vs-update ambiguity for app-assigned ids. Only {@code status}/{@code updated_at}
 * are ever actually mutated after the initial insert (confirm/release), which the
 * {@code DO UPDATE SET} clause reflects.
 */
interface SpringDataStockReservationRepository extends CrudRepository<StockReservationEntity, UUID> {

    Optional<StockReservationEntity> findByOrderIdAndSku(UUID orderId, String sku);

    @Modifying
    @Query("""
            INSERT INTO stock_reservation (id, order_id, sku, quantity, status, created_at, updated_at)
            VALUES (:id, :orderId, :sku, :quantity, :status, :createdAt, :updatedAt)
            ON CONFLICT (id) DO UPDATE SET
                status = EXCLUDED.status,
                updated_at = EXCLUDED.updated_at
            """)
    void upsert(@Param("id") UUID id, @Param("orderId") UUID orderId, @Param("sku") String sku,
                @Param("quantity") int quantity, @Param("status") String status,
                @Param("createdAt") Instant createdAt, @Param("updatedAt") Instant updatedAt);
}
