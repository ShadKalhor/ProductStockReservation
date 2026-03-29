package com.example.ProductStockReservation.Service;

import com.example.ProductStockReservation.Repository.ProductRepositoryJPA;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepositoryJPA productRepo;

    public ProductService(ProductRepositoryJPA productRepo){
        this.productRepo = productRepo;
    }

}
