package com.amazingco.stocking.features.product.usecases;

import com.amazingco.core.product.Product;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.product.ProductsService;
import com.amazingco.stocking.features.product.dtos.CreateProductCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private ProductsService productsService;

    @Test
    void buildsProductFromCommandAndDelegatesToService() {
        CreateProductUseCase useCase = new CreateProductUseCase(productsService);
        Sku sku = new Sku("ABC-123");
        Money price = Money.of("9.99", "USD");
        CreateProductCommand command =
                new CreateProductCommand("trace-1", sku, "Widget", "A widget", price, "Widgets");

        Product saved = Product.create(sku, "Widget", "A widget", price, "Widgets");
        when(productsService.create(any(Product.class))).thenReturn(saved);

        Product result = useCase.execute(command);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productsService).create(captor.capture());
        Product passed = captor.getValue();
        assertEquals(sku, passed.sku());
        assertEquals("Widget", passed.name());
        assertEquals("A widget", passed.description());
        assertEquals(price, passed.price());
        assertEquals("Widgets", passed.category());
        assertSame(saved, result);
    }
}
