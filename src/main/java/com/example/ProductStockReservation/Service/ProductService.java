package com.example.ProductStockReservation.Service;

import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Repository.ProductRepositoryJPA;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepositoryJPA productRepo;

    public ProductService(ProductRepositoryJPA productRepo){
        this.productRepo = productRepo;
    }

    public Product reserveProduct() {
        return null;
    }

    public Page<Product> findAllPageable(Pageable pageable) {
    }

    public void save(@Valid Product product) {
    }
}
