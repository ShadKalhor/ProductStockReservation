package com.example.ProductStockReservation.Repository;

import com.example.ProductStockReservation.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepositoryJPA extends JpaRepository<Product, UUID> {
}
