package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.usecase.UseCase;
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
