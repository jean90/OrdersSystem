package com.amazingco.orders.features.order.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

/**
 * The actual Spring Data JPA adapter behind {@link OrderRepositoryImpl}'s idempotency-key
 * methods. {@code insert} is a genuine SQL {@code INSERT}, deliberately not
 * {@link JpaRepository#save}: {@code save()} on an entity whose id is already set falls back to
 * {@code merge()}, which — unlike a raw {@code INSERT} hitting the primary key constraint —
 * silently *updates* the existing row on a duplicate key instead of failing. That would let a
 * replayed {@code Idempotency-Key} silently repoint to a different order.
 */
interface SpringDataOrderIdempotencyKeyRepository extends JpaRepository<OrderIdempotencyKeyEntity, String> {

    @Modifying
    @Query(value = """
            INSERT INTO order_idempotency_key (idempotency_key, order_id, created_at)
            VALUES (:idempotencyKey, :orderId, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("idempotencyKey") String idempotencyKey, @Param("orderId") UUID orderId,
                @Param("createdAt") Instant createdAt);
}
