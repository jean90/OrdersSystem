package com.amazingco.stocking.stock;

import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StockReservationsRepositoryImpl implements StockReservationsRepository {

    private final SpringDataStockReservationRepository springDataStockReservationRepository;

    public StockReservationsRepositoryImpl(SpringDataStockReservationRepository springDataStockReservationRepository) {
        this.springDataStockReservationRepository = springDataStockReservationRepository;
    }

    @Override
    public Optional<StockReservation> findByOrderIdAndSku(OrderId orderId, Sku sku) {
        return springDataStockReservationRepository.findByOrderIdAndSku(orderId.value(), sku.value())
                .map(StockReservationEntity::toDomain);
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        StockReservationEntity entity = StockReservationEntity.fromDomain(reservation);
        springDataStockReservationRepository.upsert(entity.id(), entity.orderId(), entity.sku(), entity.quantity(),
                entity.status(), entity.createdAt(), entity.updatedAt());
        return reservation;
    }
}
