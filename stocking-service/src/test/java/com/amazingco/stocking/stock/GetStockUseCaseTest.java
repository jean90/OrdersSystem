package com.amazingco.stocking.stock;

import com.amazingco.core.stock.Quantity;
import com.amazingco.core.stock.Stock;
import com.amazingco.core.stock.StockNotFoundException;
import com.amazingco.core.valueobject.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStockUseCaseTest {

    @Mock
    private StockService stockService;

    @Test
    void delegatesToServiceFindBySku() {
        GetStockUseCase useCase = new GetStockUseCase(stockService);
        Sku sku = new Sku("ABC-123");
        Stock stock = Stock.initial(sku, Quantity.of(10));
        when(stockService.findBySku(sku)).thenReturn(stock);

        Stock result = useCase.execute(new GetStockCommand("trace-1", sku));

        assertSame(stock, result);
    }

    @Test
    void propagatesStockNotFoundExceptionFromService() {
        GetStockUseCase useCase = new GetStockUseCase(stockService);
        Sku sku = new Sku("ABC-123");
        when(stockService.findBySku(sku)).thenThrow(new StockNotFoundException(sku));

        assertThrows(StockNotFoundException.class, () -> useCase.execute(new GetStockCommand("trace-1", sku)));
    }
}
