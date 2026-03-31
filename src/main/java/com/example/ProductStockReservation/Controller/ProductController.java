package com.example.ProductStockReservation.Controller;

import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.ErrorStructureException;
import com.example.ProductStockReservation.Service.ProductService;
import com.example.ProductStockReservation.dto.CreateProduct;
import com.example.ProductStockReservation.dto.ProductReservation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product postProduct(@RequestBody @Valid CreateProduct createProduct){

        Product product= new Product(null,createProduct.name(),createProduct.stock(),createProduct.version());

        return productService.save(product).getOrElseThrow(ErrorStructureException::new);

    }
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<Product> getProducts(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int size,
                                     @RequestParam(defaultValue = "id") String sortBy,
                                     @RequestParam(defaultValue = "0") boolean ascending){

        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page,size, sort);

        return productService.findAllPageable(pageable).getOrElseThrow(ErrorStructureException::new);
    }
    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.OK)
    public Product reserveProduct(@RequestBody @Valid ProductReservation productReservation){
        return productService.reserveProduct(productReservation).getOrElseThrow(ErrorStructureException::new);
    }




}
