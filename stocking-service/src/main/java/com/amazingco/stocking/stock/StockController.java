package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.web.TraceIds;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final InitializeStockUseCase initializeStockUseCase;
    private final GetStockUseCase getStockUseCase;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ConfirmStockReservationUseCase confirmStockReservationUseCase;
    private final ReleaseStockReservationUseCase releaseStockReservationUseCase;

    public StockController(InitializeStockUseCase initializeStockUseCase, GetStockUseCase getStockUseCase,
                            ReserveStockUseCase reserveStockUseCase,
                            ConfirmStockReservationUseCase confirmStockReservationUseCase,
                            ReleaseStockReservationUseCase releaseStockReservationUseCase) {
        this.initializeStockUseCase = initializeStockUseCase;
        this.getStockUseCase = getStockUseCase;
        this.reserveStockUseCase = reserveStockUseCase;
        this.confirmStockReservationUseCase = confirmStockReservationUseCase;
        this.releaseStockReservationUseCase = releaseStockReservationUseCase;
    }

    @PostMapping
    public ResponseEntity<StockResponse> initialize(
            @RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
            @Valid @RequestBody InitializeStockRequest request) {
        Stock stock = initializeStockUseCase.execute(new InitializeStockCommand(TraceIds.resolve(traceIdHeader),
                new Sku(request.sku()), Quantity.of(request.initialAvailable())));
        return ResponseEntity.created(URI.create("/api/stock/" + stock.sku().value()))
                .body(StockResponse.from(stock));
    }

    @GetMapping("/{sku}")
    public StockResponse getBySku(@RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
                                   @PathVariable("sku") String sku) {
        Stock stock = getStockUseCase.execute(new GetStockCommand(TraceIds.resolve(traceIdHeader), new Sku(sku)));
        return StockResponse.from(stock);
    }

    @PostMapping("/{sku}/reservations")
    public ResponseEntity<StockReservationResponse> reserve(
            @RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
            @PathVariable("sku") String sku, @Valid @RequestBody ReserveStockRequest request) {
        StockReservation reservation = reserveStockUseCase.execute(new ReserveStockCommand(
                TraceIds.resolve(traceIdHeader), new OrderId(request.orderId()), new Sku(sku),
                Quantity.of(request.quantity())));
        URI location = URI.create("/api/stock/" + sku + "/reservations/" + reservation.orderId().value());
        return ResponseEntity.created(location).body(StockReservationResponse.from(reservation));
    }

    @PostMapping("/{sku}/reservations/{orderId}/confirm")
    public StockReservationResponse confirm(
            @RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
            @PathVariable("sku") String sku, @PathVariable("orderId") UUID orderId) {
        StockReservation reservation = confirmStockReservationUseCase.execute(new ConfirmStockReservationCommand(
                TraceIds.resolve(traceIdHeader), new OrderId(orderId), new Sku(sku)));
        return StockReservationResponse.from(reservation);
    }

    @PostMapping("/{sku}/reservations/{orderId}/release")
    public StockReservationResponse release(
            @RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
            @PathVariable("sku") String sku, @PathVariable("orderId") UUID orderId) {
        StockReservation reservation = releaseStockReservationUseCase.execute(new ReleaseStockReservationCommand(
                TraceIds.resolve(traceIdHeader), new OrderId(orderId), new Sku(sku)));
        return StockReservationResponse.from(reservation);
    }
}
