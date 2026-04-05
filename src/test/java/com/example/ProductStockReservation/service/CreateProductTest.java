package com.example.ProductStockReservation.service;

import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.ErrorType;
import com.example.ProductStockReservation.Repository.ProductRepositoryJPA;
import com.example.ProductStockReservation.Service.ProductService;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductTest {

    @Mock
    private ProductRepositoryJPA productRepositoryJPA;

    @Mock
    private LockingTaskExecutor executor;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        Product product = new Product(UUID.randomUUID(), "Laptop", 20);

        when(productRepositoryJPA.save(product)).thenReturn(product);

        var result = productService.save(product);

        assertThat(result).isRight();
        assertThat(result.get()).isEqualTo(product);
        verify(productRepositoryJPA).save(product);
    }

    @Test
    void shouldReturnServerErrorWhenRepositoryThrowsWhileCreatingProduct() {
        Product product = new Product(UUID.randomUUID(), "Laptop", 20);

        when(productRepositoryJPA.save(product)).thenThrow(new RuntimeException("db down"));

        var result = productService.save(product);

        assertThat(result).isLeft();
        assertThat(result).hasLeftValueSatisfying(error -> {
            org.assertj.core.api.Assertions.assertThat(error.message()).isEqualTo("Issue when Saving The Product");
            org.assertj.core.api.Assertions.assertThat(error.type()).isEqualTo(ErrorType.SERVER_ERROR);
        });
    }
}