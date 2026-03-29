package com.example.ProductStockReservation.Repository;

import com.example.ProductStockReservation.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepositoryJPA extends JpaRepository<Product, UUID> {
}
