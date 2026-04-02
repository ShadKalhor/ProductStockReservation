package com.example.ProductStockReservation.Repository;

import com.example.ProductStockReservation.Entity.Product;
import io.vavr.control.Option;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepositoryJPA extends JpaRepository<Product, UUID> {

    Option<Product> findOptionById(UUID id);


}
