package com.amazingco.orders.features.order.web;

import com.amazingco.core.order.Order;
import com.amazingco.core.order.OrderNotFoundException;
import com.amazingco.core.stock.Quantity;
import com.amazingco.core.valueobject.CustomerId;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.OrderId;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.orders.features.order.dtos.CreateOrderCommand;
import com.amazingco.orders.features.order.dtos.GetOrderCommand;
import com.amazingco.orders.features.order.usecases.CreateOrderUseCase;
import com.amazingco.orders.features.order.usecases.GetOrderUseCase;
import com.amazingco.orders.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    private static final CustomerId CUSTOMER_ID = CustomerId.newId();
    private static final Sku SKU = new Sku("ABC-123");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;
    @MockBean
    private GetOrderUseCase getOrderUseCase;

    private static Order anOrder() {
        Order order = Order.create(CUSTOMER_ID);
        order.addLine(SKU, Quantity.of(2), Money.of("9.99", "USD"));
        return order;
    }

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        Order order = anOrder();
        when(createOrderUseCase.execute(any(CreateOrderCommand.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "key-1")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","lines":[
                                  {"sku":"ABC-123","quantity":2,"unitPrice":9.99,"currency":"USD"}
                                ]}
                                """.formatted(CUSTOMER_ID.value())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/" + order.orderId().value()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.totalAmount").value(19.98));
    }

    @Test
    void createWithoutIdempotencyKeyHeaderReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","lines":[
                                  {"sku":"ABC-123","quantity":2,"unitPrice":9.99,"currency":"USD"}
                                ]}
                                """.formatted(CUSTOMER_ID.value())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithBlankIdempotencyKeyReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "   ")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","lines":[
                                  {"sku":"ABC-123","quantity":2,"unitPrice":9.99,"currency":"USD"}
                                ]}
                                """.formatted(CUSTOMER_ID.value())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithEmptyLinesListReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "key-1")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"%s","lines":[]}
                                """.formatted(CUSTOMER_ID.value())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithInvalidCustomerIdReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "key-1")
                        .contentType("application/json")
                        .content("""
                                {"customerId":"not-a-uuid","lines":[
                                  {"sku":"ABC-123","quantity":2,"unitPrice":9.99,"currency":"USD"}
                                ]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturns200() throws Exception {
        Order order = anOrder();
        when(getOrderUseCase.execute(any(GetOrderCommand.class))).thenReturn(order);

        mockMvc.perform(get("/api/orders/{id}", order.orderId().value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.orderId().value().toString()));
    }

    @Test
    void getByIdMissingReturns404() throws Exception {
        OrderId orderId = OrderId.newId();
        when(getOrderUseCase.execute(any(GetOrderCommand.class))).thenThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(get("/api/orders/{id}", orderId.value()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdWithInvalidUuidReturns400() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
