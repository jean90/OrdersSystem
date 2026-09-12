package com.amazingco.stocking.features.stock.web;

import com.amazingco.core.stock.InsufficientStockException;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.stock.StockNotFoundException;
import com.amazingco.core.stock.StockReservation;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.stock.StockAlreadyExistsException;
import com.amazingco.stocking.features.stock.dtos.ConfirmStockReservationCommand;
import com.amazingco.stocking.features.stock.dtos.GetStockCommand;
import com.amazingco.stocking.features.stock.dtos.InitializeStockCommand;
import com.amazingco.stocking.features.stock.dtos.ReleaseStockReservationCommand;
import com.amazingco.stocking.features.stock.dtos.ReserveStockCommand;
import com.amazingco.stocking.features.stock.usecases.ConfirmStockReservationUseCase;
import com.amazingco.stocking.features.stock.usecases.GetStockUseCase;
import com.amazingco.stocking.features.stock.usecases.InitializeStockUseCase;
import com.amazingco.stocking.features.stock.usecases.ReleaseStockReservationUseCase;
import com.amazingco.stocking.features.stock.usecases.ReserveStockUseCase;
import com.amazingco.stocking.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@Import(GlobalExceptionHandler.class)
class StockControllerTest {

    private static final Sku SKU = new Sku("ABC-123");
    private static final OrderId ORDER_ID = OrderId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InitializeStockUseCase initializeStockUseCase;
    @MockBean
    private GetStockUseCase getStockUseCase;
    @MockBean
    private ReserveStockUseCase reserveStockUseCase;
    @MockBean
    private ConfirmStockReservationUseCase confirmStockReservationUseCase;
    @MockBean
    private ReleaseStockReservationUseCase releaseStockReservationUseCase;

    private static StockReservation reservation() {
        return StockReservation.create(ORDER_ID, SKU, Quantity.of(3));
    }

    @Test
    void initializeReturns201() throws Exception {
        when(initializeStockUseCase.execute(any(InitializeStockCommand.class)))
                .thenReturn(Stock.initial(SKU, Quantity.of(20)));

        mockMvc.perform(post("/api/stock")
                        .contentType("application/json")
                        .content("""
                                {"sku":"ABC-123","initialAvailable":20}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("ABC-123"))
                .andExpect(jsonPath("$.available").value(20))
                .andExpect(jsonPath("$.reserved").value(0));
    }

    @Test
    void initializeWithExistingSkuReturns409() throws Exception {
        when(initializeStockUseCase.execute(any(InitializeStockCommand.class)))
                .thenThrow(new StockAlreadyExistsException(SKU));

        mockMvc.perform(post("/api/stock")
                        .contentType("application/json")
                        .content("""
                                {"sku":"ABC-123","initialAvailable":20}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void getBySkuReturns200() throws Exception {
        when(getStockUseCase.execute(any(GetStockCommand.class))).thenReturn(Stock.initial(SKU, Quantity.of(10)));

        mockMvc.perform(get("/api/stock/{sku}", "ABC-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(10));
    }

    @Test
    void getBySkuMissingReturns404() throws Exception {
        when(getStockUseCase.execute(any(GetStockCommand.class))).thenThrow(new StockNotFoundException(SKU));

        mockMvc.perform(get("/api/stock/{sku}", "ABC-123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reserveReturns201() throws Exception {
        when(reserveStockUseCase.execute(any(ReserveStockCommand.class))).thenReturn(reservation());

        mockMvc.perform(post("/api/stock/{sku}/reservations", "ABC-123")
                        .contentType("application/json")
                        .content("{\"orderId\":\"" + ORDER_ID.value() + "\",\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void reserveWithInsufficientStockReturns409() throws Exception {
        when(reserveStockUseCase.execute(any(ReserveStockCommand.class)))
                .thenThrow(new InsufficientStockException(SKU, Quantity.of(3), Quantity.of(1)));

        mockMvc.perform(post("/api/stock/{sku}/reservations", "ABC-123")
                        .contentType("application/json")
                        .content("{\"orderId\":\"" + UUID.randomUUID() + "\",\"quantity\":3}"))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmReturns200() throws Exception {
        StockReservation reservation = reservation();
        reservation.confirm();
        when(confirmStockReservationUseCase.execute(any(ConfirmStockReservationCommand.class)))
                .thenReturn(reservation);

        mockMvc.perform(post("/api/stock/{sku}/reservations/{orderId}/confirm", "ABC-123", ORDER_ID.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void releaseReturns200() throws Exception {
        StockReservation reservation = reservation();
        reservation.release();
        when(releaseStockReservationUseCase.execute(any(ReleaseStockReservationCommand.class)))
                .thenReturn(reservation);

        mockMvc.perform(post("/api/stock/{sku}/reservations/{orderId}/release", "ABC-123", ORDER_ID.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));
    }
}
