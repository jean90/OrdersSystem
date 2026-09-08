package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductUseCaseTest {

    @Mock
    private ProductsService productsService;

    @Test
    void delegatesToServiceFindBySku() {
        GetProductUseCase useCase = new GetProductUseCase(productsService);
        Sku sku = new Sku("ABC-123");
        Product product = Product.create(sku, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets");
        when(productsService.findBySku(sku)).thenReturn(product);

        Product result = useCase.execute(new GetProductCommand("trace-1", sku));

        assertSame(product, result);
    }
}
