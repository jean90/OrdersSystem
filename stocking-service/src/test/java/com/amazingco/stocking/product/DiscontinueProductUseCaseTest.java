package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.product.ProductStatus;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscontinueProductUseCaseTest {

    @Mock
    private ProductsService productsService;

    @Test
    void loadsDiscontinuesAndPersistsProduct() {
        DiscontinueProductUseCase useCase = new DiscontinueProductUseCase(productsService);
        Sku sku = new Sku("ABC-123");
        Product existing = Product.create(sku, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets");
        when(productsService.findBySku(sku)).thenReturn(existing);
        when(productsService.update(existing)).thenReturn(existing);

        DiscontinueProductCommand command = new DiscontinueProductCommand("trace-1", sku);

        Product result = useCase.execute(command);

        assertEquals(ProductStatus.DISCONTINUED, existing.status());
        verify(productsService).update(existing);
        assertSame(existing, result);
    }
}
