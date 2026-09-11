package com.amazingco.stocking.product;

import com.amazingco.core.product.Product;
import com.amazingco.core.valueobject.Money;
import com.amazingco.core.valueobject.Sku;
import com.amazingco.stocking.web.TraceIds;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DiscontinueProductUseCase discontinueProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase, GetProductUseCase getProductUseCase,
                              ListProductsUseCase listProductsUseCase, UpdateProductUseCase updateProductUseCase,
                              DiscontinueProductUseCase discontinueProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.discontinueProductUseCase = discontinueProductUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
            @Valid @RequestBody CreateProductRequest request) {
        String traceId = TraceIds.resolve(traceIdHeader);
        Money price = new Money(request.price(), Currency.getInstance(request.currency()));
        Product product = createProductUseCase.execute(new CreateProductCommand(traceId, new Sku(request.sku()),
                request.name(), request.description(), price, request.category()));
        return ResponseEntity.created(URI.create("/api/products/" + product.sku().value()))
                .body(ProductResponse.from(product));
    }

    @GetMapping("/{sku}")
    public ProductResponse getBySku(@RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
                                     @PathVariable("sku") String sku) {
        Product product =
                getProductUseCase.execute(new GetProductCommand(TraceIds.resolve(traceIdHeader), new Sku(sku)));
        return ProductResponse.from(product);
    }

    @GetMapping
    public List<ProductResponse> list(@RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader) {
        return listProductsUseCase.execute(new ListProductsCommand(TraceIds.resolve(traceIdHeader))).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @PutMapping("/{sku}")
    public ProductResponse update(@RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
                                   @PathVariable("sku") String sku, @Valid @RequestBody UpdateProductRequest request) {
        Money price = new Money(request.price(), Currency.getInstance(request.currency()));
        Product product = updateProductUseCase.execute(new UpdateProductCommand(TraceIds.resolve(traceIdHeader),
                new Sku(sku), request.name(), request.description(), price, request.category()));
        return ProductResponse.from(product);
    }

    @PostMapping("/{sku}/discontinue")
    public ProductResponse discontinue(@RequestHeader(value = TraceIds.HEADER, required = false) String traceIdHeader,
                                        @PathVariable("sku") String sku) {
        Product product = discontinueProductUseCase.execute(
                new DiscontinueProductCommand(TraceIds.resolve(traceIdHeader), new Sku(sku)));
        return ProductResponse.from(product);
    }
}
