package com.amazingco.orders.features.ordertransaction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * The actual Spring Data JPA adapter behind {@link OrderTransactionRepositoryImpl}. Plain
 * {@code save()} is correct here — see {@link OrderTransactionEntity}'s Javadoc.
 */
interface SpringDataOrderTransactionRepository extends JpaRepository<OrderTransactionEntity, UUID> {
}
