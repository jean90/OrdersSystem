package com.amazingco.stocking.features.product.usecases;

import com.amazingco.core.product.Product;
import com.amazingco.core.usecase.UseCase;
import com.amazingco.stocking.features.product.ProductsService;
import com.amazingco.stocking.features.product.dtos.ListProductsCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListProductsUseCase implements UseCase<ListProductsCommand, List<Product>> {

    private final ProductsService productsService;

    public ListProductsUseCase(ProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> execute(ListProductsCommand command) {
        return productsService.findAll();
    }
}
