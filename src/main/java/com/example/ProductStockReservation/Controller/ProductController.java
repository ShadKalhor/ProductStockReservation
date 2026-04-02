package com.example.ProductStockReservation.Controller;

import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.ErrorStructureException;
import com.example.ProductStockReservation.Exception.ErrorType;
import com.example.ProductStockReservation.Exception.StructuredError;
import com.example.ProductStockReservation.Service.ProductService;
import com.example.ProductStockReservation.dto.CreateProduct;
import com.example.ProductStockReservation.dto.ProductReservation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product postProduct(@RequestBody @Valid CreateProduct createProduct){
        log.debug("Create Product Request Received. ProductName: {}, ProductStock: {}",
                createProduct.name(), createProduct.stock());


        log.debug("Mapping CreateProduct DTO to Product Entity. ProductName: {}, ProductStock: {}",
                createProduct.name(), createProduct.stock());

        Product product= new Product(null,createProduct.name(),createProduct.stock(), 1000L);

        return productService.save(product).peekLeft(error -> log.error("Unexpected Error Receiving Result From Create Product Service.")).getOrElseThrow(ErrorStructureException::new);

    }
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<Product> getProducts(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int size,
                                     @RequestParam(defaultValue = "id") String sortBy,
                                     @RequestParam(defaultValue = "true") boolean ascending){

        log.debug("Get Product Request Received.");


        log.debug("Preparing Sort Configuration. Page: {}, Size: {}, SortBy: {}, IsAscending: {}",
                page,
                size,
                sortBy,
                ascending);

        if (!sortBy.equals("id") && !sortBy.equals("name") && !sortBy.equals("stock")&& !sortBy.equals("version")) {

            log.error("Invalid Sort Field. SortBy: {}", sortBy);
            throw new ErrorStructureException(new StructuredError("Invalid Sort Field.", ErrorType.VALIDATION_ERROR));
        }

        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page,size,sort);

        return productService.findAllPageable(pageable).peekLeft(error -> log.error("Unexpected Error Receiving Result From Get Products Service.")).getOrElseThrow(ErrorStructureException::new);
    }
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.OK)
    public Product reserveProduct(@RequestBody @Valid ProductReservation productReservation){

        log.debug("Reserve Product Request Received. ProductId: {}, ReservingQuantity: {}",
                productReservation.id(), productReservation.quantity());

        return productService.reserveProduct(productReservation).peekLeft(error -> log.error("Unexpected Error Receiving Result From Reserve Product Service.")).getOrElseThrow(ErrorStructureException::new);
    }




}
