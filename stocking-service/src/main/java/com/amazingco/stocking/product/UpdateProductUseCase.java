package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.usecase.UseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProductUseCase implements UseCase<UpdateProductCommand, Product> {

    private final ProductsService productsService;

    public UpdateProductUseCase(ProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    @Transactional
    public Product execute(UpdateProductCommand command) {
        Product product = productsService.findBySku(command.sku());
        product.rename(command.name());
        product.reprice(command.price());
        product.changeCategory(command.category());
        product.redescribe(command.description());
        return productsService.update(product);
    }
}
