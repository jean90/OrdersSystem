package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.valueobject.Sku;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class ProductsRepositoryImpl implements ProductsRepository {

    private final SpringDataProductRepository springDataProductRepository;

    public ProductsRepositoryImpl(SpringDataProductRepository springDataProductRepository) {
        this.springDataProductRepository = springDataProductRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        springDataProductRepository.upsert(entity.sku(), entity.name(), entity.description(), entity.price(),
                entity.currency(), entity.category(), entity.status(), entity.createdAt(), entity.updatedAt());
        return product;
    }

    @Override
    public Optional<Product> findBySku(Sku sku) {
        return springDataProductRepository.findById(sku.value()).map(ProductEntity::toDomain);
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return springDataProductRepository.existsById(sku.value());
    }

    @Override
    public List<Product> findAll() {
        return StreamSupport.stream(springDataProductRepository.findAll().spliterator(), false)
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteBySku(Sku sku) {
        springDataProductRepository.deleteById(sku.value());
    }
}
