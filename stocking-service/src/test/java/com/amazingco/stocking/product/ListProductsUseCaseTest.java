package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductsUseCaseTest {

    @Mock
    private ProductsService productsService;

    @Test
    void delegatesToServiceFindAll() {
        ListProductsUseCase useCase = new ListProductsUseCase(productsService);
        Sku sku = new Sku("ABC-123");
        List<Product> products = List.of(Product.create(sku, "Widget", "A widget",
                Money.of("9.99", "USD"), "Widgets"));
        when(productsService.findAll()).thenReturn(products);

        List<Product> result = useCase.execute(new ListProductsCommand("trace-1"));

        assertEquals(products, result);
    }
}
