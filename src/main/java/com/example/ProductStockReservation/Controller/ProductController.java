package com.example.ProductStockReservation.Controller;

import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void postProduct(@RequestBody @Valid Product product){

        productService.save(product);

    }
    @GetMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Page<Product> postProduct(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "0") int size,
                                     @RequestParam(defaultValue = "id") String sortBy,
                                     @RequestParam(defaultValue = "0") boolean ascending){

        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page,size, sort);
        return productService.findAllPageable(pageable);
    }
    @PostMapping("{id}/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public Product reserveProduct(@PathVariable UUID id,
            @RequestParam int quantity){

        return productService.reserveProduct();

    }




}
