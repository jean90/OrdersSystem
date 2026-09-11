package com.amazingco.stocking.features.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.product.ProductNotFoundException;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.features.product.repositories.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsServiceImpl implements ProductsService {

    private final ProductsRepository productsRepository;

    public ProductsServiceImpl(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @Override
    public Product create(Product product) {
        if (productsRepository.existsBySku(product.sku())) {
            throw new ProductAlreadyExistsException(product.sku());
        }
        return productsRepository.save(product);
    }

    @Override
    public Product findBySku(Sku sku) {
        return productsRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Override
    public Product update(Product product) {
        return productsRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productsRepository.findAll();
    }

    @Override
    public void delete(Sku sku) {
        findBySku(sku);
        productsRepository.deleteBySku(sku);
    }
}
