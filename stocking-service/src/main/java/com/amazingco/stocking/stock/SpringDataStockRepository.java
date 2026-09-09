package com.amazingco.stocking.stock;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The actual Spring Data JDBC adapter behind {@link StockRepositoryImpl}. Every write is
 * a single atomic conditional {@code UPDATE} guarded by the affected row's own current
 * value — never a read-modify-write of {@link StockEntity} — per the concurrency
 * architecture note. Each returns the number of rows affected (0 means the guard failed
 * or the SKU is unknown); {@link StockRepositoryImpl} turns that into the port's contract.
 */
interface SpringDataStockRepository extends CrudRepository<StockEntity, String> {

    @Modifying
    @Query("""
            UPDATE stock SET available = available - :quantity, reserved = reserved + :quantity
            WHERE sku = :sku AND available >= :quantity
            """)
    int tryReserve(@Param("sku") String sku, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE stock SET reserved = reserved - :quantity WHERE sku = :sku AND reserved >= :quantity")
    int confirmReservation(@Param("sku") String sku, @Param("quantity") int quantity);

    @Modifying
    @Query("""
            UPDATE stock SET reserved = reserved - :quantity, available = available + :quantity
            WHERE sku = :sku AND reserved >= :quantity
            """)
    int releaseReservation(@Param("sku") String sku, @Param("quantity") int quantity);
}
