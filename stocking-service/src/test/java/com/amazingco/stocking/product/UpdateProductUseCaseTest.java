package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
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
class UpdateProductUseCaseTest {

    @Mock
    private ProductsService productsService;

    @Test
    void loadsMutatesAndPersistsProduct() {
        UpdateProductUseCase useCase = new UpdateProductUseCase(productsService);
        Sku sku = new Sku("ABC-123");
        Product existing = Product.create(sku, "Old Name", "Old desc", Money.of("9.99", "USD"), "OldCat");
        when(productsService.findBySku(sku)).thenReturn(existing);
        when(productsService.update(existing)).thenReturn(existing);

        Money newPrice = Money.of("19.99", "USD");
        UpdateProductCommand command =
                new UpdateProductCommand("trace-1", sku, "New Name", "New desc", newPrice, "NewCat");

        Product result = useCase.execute(command);

        assertEquals("New Name", existing.name());
        assertEquals("New desc", existing.description());
        assertEquals(newPrice, existing.price());
        assertEquals("NewCat", existing.category());
        verify(productsService).update(existing);
        assertSame(existing, result);
    }
}
