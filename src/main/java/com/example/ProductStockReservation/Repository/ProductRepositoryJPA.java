package com.example.ProductStockReservation.Repository;

import com.example.ProductStockReservation.Entity.Product;
import io.vavr.control.Option;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepositoryJPA extends JpaRepository<Product, UUID> {

    @Override
    @SchedulerLock(name = "product_save_lock", lockAtMostFor = "5m", lockAtLeastFor = "1m")
    <S extends Product> S save(S product);

    Option<Product> findOptionById(UUID id);


}
