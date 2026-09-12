package com.amazingco.stocking.features.product.usecases;

import com.amazingco.core.product.Product;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.product.ProductsService;
import com.amazingco.stocking.features.product.dtos.GetProductCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetProductUseCase implements UseCase<GetProductCommand, Product> {

    private final ProductsService productsService;

    public GetProductUseCase(ProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    @Transactional(readOnly = true)
    public Product execute(GetProductCommand command) {
        return productsService.findBySku(command.sku());
    }
}
