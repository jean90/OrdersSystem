package com.amazingco.stocking.features.product.web;

import com.amazingco.core.product.Product;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.product.ProductAlreadyExistsException;
import com.amazingco.stocking.features.product.dtos.CreateProductCommand;
import com.amazingco.stocking.features.product.dtos.DiscontinueProductCommand;
import com.amazingco.stocking.features.product.dtos.GetProductCommand;
import com.amazingco.stocking.features.product.dtos.ListProductsCommand;
import com.amazingco.stocking.features.product.dtos.UpdateProductCommand;
import com.amazingco.stocking.features.product.usecases.CreateProductUseCase;
import com.amazingco.stocking.features.product.usecases.DiscontinueProductUseCase;
import com.amazingco.stocking.features.product.usecases.GetProductUseCase;
import com.amazingco.stocking.features.product.usecases.ListProductsUseCase;
import com.amazingco.stocking.features.product.usecases.UpdateProductUseCase;
import com.amazingco.stocking.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    private static final Sku SKU = new Sku("ABC-123");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateProductUseCase createProductUseCase;
    @MockBean
    private GetProductUseCase getProductUseCase;
    @MockBean
    private ListProductsUseCase listProductsUseCase;
    @MockBean
    private UpdateProductUseCase updateProductUseCase;
    @MockBean
    private DiscontinueProductUseCase discontinueProductUseCase;

    private static Product product() {
        return Product.create(SKU, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets");
    }

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(createProductUseCase.execute(any(CreateProductCommand.class))).thenReturn(product());

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("""
                                {"sku":"ABC-123","name":"Widget","description":"A widget",
                                 "price":9.99,"currency":"USD","category":"Widgets"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/ABC-123"))
                .andExpect(jsonPath("$.sku").value("ABC-123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("""
                                {"sku":"ABC-123","name":"","description":"A widget",
                                 "price":9.99,"currency":"USD","category":"Widgets"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithExistingSkuReturns409() throws Exception {
        when(createProductUseCase.execute(any(CreateProductCommand.class)))
                .thenThrow(new ProductAlreadyExistsException(SKU));

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("""
                                {"sku":"ABC-123","name":"Widget","description":"A widget",
                                 "price":9.99,"currency":"USD","category":"Widgets"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void getBySkuReturns200() throws Exception {
        when(getProductUseCase.execute(any(GetProductCommand.class))).thenReturn(product());

        mockMvc.perform(get("/api/products/{sku}", "ABC-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("ABC-123"));
    }

    @Test
    void getBySkuMissingReturns404() throws Exception {
        when(getProductUseCase.execute(any(GetProductCommand.class)))
                .thenThrow(new com.amazingco.core.product.ProductNotFoundException(SKU));

        mockMvc.perform(get("/api/products/{sku}", "ABC-123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturns200WithArray() throws Exception {
        when(listProductsUseCase.execute(any(ListProductsCommand.class))).thenReturn(List.of(product()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("ABC-123"));
    }

    @Test
    void updateReturns200() throws Exception {
        when(updateProductUseCase.execute(any(UpdateProductCommand.class))).thenReturn(product());

        mockMvc.perform(put("/api/products/{sku}", "ABC-123")
                        .contentType("application/json")
                        .content("""
                                {"name":"Widget","description":"A widget",
                                 "price":9.99,"currency":"USD","category":"Widgets"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void discontinueReturns200() throws Exception {
        when(discontinueProductUseCase.execute(any(DiscontinueProductCommand.class))).thenReturn(product());

        mockMvc.perform(post("/api/products/{sku}/discontinue", "ABC-123"))
                .andExpect(status().isOk());
    }
}
