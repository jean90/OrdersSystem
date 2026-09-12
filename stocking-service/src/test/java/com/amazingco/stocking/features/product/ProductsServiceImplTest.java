package com.amazingco.stocking.features.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.product.ProductNotFoundException;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.product.repositories.ProductsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductsServiceImplTest {

    private static final Sku SKU = new Sku("ABC-123");

    @Mock
    private ProductsRepository productsRepository;

    private ProductsService productsService;

    @BeforeEach
    void setUp() {
        productsService = new ProductsServiceImpl(productsRepository);
    }

    private Product aProduct() {
        return Product.create(SKU, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets");
    }

    @Test
    void createSavesWhenSkuNotAlreadyRegistered() {
        Product product = aProduct();
        when(productsRepository.existsBySku(SKU)).thenReturn(false);
        when(productsRepository.save(product)).thenReturn(product);

        Product result = productsService.create(product);

        assertSame(product, result);
        verify(productsRepository).save(product);
    }

    @Test
    void createRejectsDuplicateSku() {
        Product product = aProduct();
        when(productsRepository.existsBySku(SKU)).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productsService.create(product));

        verify(productsRepository, never()).save(product);
    }

    @Test
    void findBySkuReturnsProductWhenPresent() {
        Product product = aProduct();
        when(productsRepository.findBySku(SKU)).thenReturn(Optional.of(product));

        assertSame(product, productsService.findBySku(SKU));
    }

    @Test
    void findBySkuThrowsWhenAbsent() {
        when(productsRepository.findBySku(SKU)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productsService.findBySku(SKU));
    }

    @Test
    void updateSavesWithoutRecheckingExistence() {
        Product product = aProduct();
        when(productsRepository.save(product)).thenReturn(product);

        Product result = productsService.update(product);

        assertSame(product, result);
        verify(productsRepository, never()).existsBySku(SKU);
    }

    @Test
    void findAllDelegatesToRepository() {
        List<Product> products = List.of(aProduct());
        when(productsRepository.findAll()).thenReturn(products);

        assertEquals(products, productsService.findAll());
    }

    @Test
    void deleteRemovesWhenProductExists() {
        Product product = aProduct();
        when(productsRepository.findBySku(SKU)).thenReturn(Optional.of(product));

        productsService.delete(SKU);

        verify(productsRepository).deleteBySku(SKU);
    }

    @Test
    void deleteThrowsWhenProductAbsent() {
        when(productsRepository.findBySku(SKU)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productsService.delete(SKU));

        verify(productsRepository, never()).deleteBySku(SKU);
    }
}
