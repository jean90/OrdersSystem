package com.amazingco.stocking.features.product.repositories;

import com.amazingco.core.product.Product;
import com.amazingco.core.product.ProductStatus;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductsRepositoryImplIT extends AbstractPostgresIntegrationTest {

    private static final Sku SKU = new Sku("ABC-123");

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM product");
    }

    @Test
    void saveThenFindBySkuRoundTripsAllFields() {
        Product product = Product.create(SKU, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets");

        productsRepository.save(product);

        Product found = productsRepository.findBySku(SKU).orElseThrow();
        assertEquals(SKU, found.sku());
        assertEquals("Widget", found.name());
        assertEquals("A widget", found.description());
        assertEquals(Money.of("9.99", "USD"), found.price());
        assertEquals("Widgets", found.category());
        assertEquals(ProductStatus.ACTIVE, found.status());
        // Postgres timestamptz has microsecond precision and the driver rounds rather than
        // truncates on the way in/out, so comparing at full micro precision is flaky by a
        // microsecond at the boundary. Millisecond precision is still far tighter than this
        // field needs and isn't sensitive to that rounding.
        assertEquals(product.createdAt().truncatedTo(ChronoUnit.MILLIS), found.createdAt().truncatedTo(ChronoUnit.MILLIS));
        assertEquals(product.updatedAt().truncatedTo(ChronoUnit.MILLIS), found.updatedAt().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void saveUpsertsRatherThanDuplicating() {
        Product product = Product.create(SKU, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets");
        productsRepository.save(product);

        product.rename("Better Widget");
        productsRepository.save(product);

        assertEquals(1, productsRepository.findAll().size());
        assertEquals("Better Widget", productsRepository.findBySku(SKU).orElseThrow().name());
    }

    @Test
    void existsBySkuReflectsPresence() {
        assertFalse(productsRepository.existsBySku(SKU));

        productsRepository.save(Product.create(SKU, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets"));

        assertTrue(productsRepository.existsBySku(SKU));
    }

    @Test
    void findBySkuReturnsEmptyWhenAbsent() {
        assertEquals(Optional.empty(), productsRepository.findBySku(SKU));
    }

    @Test
    void findAllReturnsEverySavedProduct() {
        productsRepository.save(Product.create(SKU, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets"));
        productsRepository.save(Product.create(new Sku("XYZ-999"), "Gadget", "A gadget",
                Money.of("19.99", "USD"), "Gadgets"));

        List<Product> all = productsRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteBySkuRemovesTheRow() {
        productsRepository.save(Product.create(SKU, "Widget", "A widget", Money.of("9.99", "USD"), "Widgets"));

        productsRepository.deleteBySku(SKU);

        assertTrue(productsRepository.findBySku(SKU).isEmpty());
    }
}
