package com.amazingco.stocking.features.product.usecases;

import com.amazingco.core.product.Product;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.product.ProductsService;
import com.amazingco.stocking.features.product.dtos.DiscontinueProductCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscontinueProductUseCase implements UseCase<DiscontinueProductCommand, Product> {

    private final ProductsService productsService;

    public DiscontinueProductUseCase(ProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    @Transactional
    public Product execute(DiscontinueProductCommand command) {
        Product product = productsService.findBySku(command.sku());
        product.discontinue();
        return productsService.update(product);
    }
}
