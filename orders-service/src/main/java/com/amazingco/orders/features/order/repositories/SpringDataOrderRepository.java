package com.amazingco.orders.features.order.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * The actual Spring Data JPA adapter behind {@link OrderRepositoryImpl}. Unlike stocking-service's
 * Spring Data JDBC repositories, plain {@code save()} handles both insert and update correctly
 * even with an application-assigned {@link UUID} id — JPA's {@code merge()} (which
 * {@code SimpleJpaRepository.save()} falls back to whenever the id is already set) looks the row
 * up by id and inserts or updates as appropriate, unlike Spring Data JDBC's insert-vs-update
 * decision, which only works for DB-generated ids.
 */
interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {
}
