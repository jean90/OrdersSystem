package com.amazingco.stocking.features.product.usecases;

import com.amazingco.core.product.Product;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.product.ProductsService;
import com.amazingco.stocking.features.product.dtos.CreateProductCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductUseCase implements UseCase<CreateProductCommand, Product> {

    private final ProductsService productsService;

    public CreateProductUseCase(ProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    @Transactional
    public Product execute(CreateProductCommand command) {
        Product product = Product.create(command.sku(), command.name(), command.description(),
                command.price(), command.category());
        return productsService.create(product);
    }
}
