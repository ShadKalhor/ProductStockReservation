package com.example.ProductStockReservation.service;

import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.ErrorType;
import com.example.ProductStockReservation.Repository.ProductRepositoryJPA;
import com.example.ProductStockReservation.Service.ProductService;
import com.example.ProductStockReservation.dto.ProductReservation;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReserveProductTest {

    @Mock
    private ProductRepositoryJPA productRepositoryJPA;

    @Mock
    private LockingTaskExecutor executor;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnLockNotAcquiredWhenTaskWasNotExecuted() throws Throwable {
        ProductReservation reservation = new ProductReservation(UUID.randomUUID(), 2);

        @SuppressWarnings("unchecked")
        LockingTaskExecutor.TaskResult<Object> taskResult =
                (LockingTaskExecutor.TaskResult<Object>) mock(LockingTaskExecutor.TaskResult.class);

        when(taskResult.wasExecuted()).thenReturn(false);
        when(executor.executeWithLock(
                any(LockingTaskExecutor.TaskWithResult.class),
                any(LockConfiguration.class)
        )).thenReturn(taskResult);

        var result = productService.reserveProduct(reservation);

        assertThat(result).isLeft();
        assertThat(result).hasLeftValueSatisfying(error -> {
            org.assertj.core.api.Assertions.assertThat(error.message()).isEqualTo("LOCK_NOT_ACQUIRED");
            org.assertj.core.api.Assertions.assertThat(error.type()).isEqualTo(ErrorType.SERVER_ERROR);
        });
    }

    @Test
    void shouldReturnServerErrorWhenExecutorThrowsException() throws Throwable {
        ProductReservation reservation = new ProductReservation(UUID.randomUUID(), 2);

        when(executor.executeWithLock(
                any(LockingTaskExecutor.TaskWithResult.class),
                any(LockConfiguration.class)
        )).thenThrow(new RuntimeException("lock failure"));

        var result = productService.reserveProduct(reservation);

        assertThat(result).isLeft();
        assertThat(result).hasLeftValueSatisfying(error -> {
            org.assertj.core.api.Assertions.assertThat(error.message()).isEqualTo("Internal Server Error Reserving Product");
            org.assertj.core.api.Assertions.assertThat(error.type()).isEqualTo(ErrorType.SERVER_ERROR);
        });
    }

    @Test
    void shouldReturnServerErrorWhenTaskExecutedButResultWasNull() throws Throwable {
        ProductReservation reservation = new ProductReservation(UUID.randomUUID(), 2);

        @SuppressWarnings("unchecked")
        LockingTaskExecutor.TaskResult<Object> taskResult =
                (LockingTaskExecutor.TaskResult<Object>) mock(LockingTaskExecutor.TaskResult.class);

        when(taskResult.wasExecuted()).thenReturn(true);
        when(taskResult.getResult()).thenReturn(null);
        when(executor.executeWithLock(
                any(LockingTaskExecutor.TaskWithResult.class),
                any(LockConfiguration.class)
        )).thenReturn(taskResult);

        var result = productService.reserveProduct(reservation);

        assertThat(result).isLeft();
        assertThat(result).hasLeftValueSatisfying(error -> {
            org.assertj.core.api.Assertions.assertThat(error.message()).isEqualTo("LOCK_EXECUTED_BUT_RESULT_NULL");
            org.assertj.core.api.Assertions.assertThat(error.type()).isEqualTo(ErrorType.SERVER_ERROR);
        });
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Throwable {
        ProductReservation reservation = new ProductReservation(UUID.randomUUID(), 2);

        @SuppressWarnings("unchecked")
        LockingTaskExecutor.TaskResult<Object> taskResult =
                (LockingTaskExecutor.TaskResult<Object>) mock(LockingTaskExecutor.TaskResult.class);

        when(taskResult.wasExecuted()).thenReturn(true);

        when(executor.executeWithLock(
                any(LockingTaskExecutor.TaskWithResult.class),
                any(LockConfiguration.class)
        )).thenAnswer(invocation -> {
            LockingTaskExecutor.TaskWithResult<?> task =
                    invocation.getArgument(0);
            Object result = task.call(); // EXECUTE REAL LOGIC

            when(taskResult.getResult()).thenReturn(result);
            return taskResult;
        });

        when(productRepositoryJPA.findById(any())).thenReturn(java.util.Optional.empty());

        var result = productService.reserveProduct(reservation);

        assertThat(result).isLeft();
        assertThat(result).hasLeftValueSatisfying(error -> {
            org.assertj.core.api.Assertions.assertThat(error.type())
                    .isEqualTo(ErrorType.NOT_FOUND_ERROR);
        });
    }

    @Test
    void shouldReturnErrorWhenStockIsInsufficient() throws Throwable {
        UUID productId = UUID.randomUUID();
        ProductReservation reservation = new ProductReservation(productId, 10);

        Product product = new Product(productId, "Laptop", 5);

        @SuppressWarnings("unchecked")
        LockingTaskExecutor.TaskResult<Object> taskResult =
                (LockingTaskExecutor.TaskResult<Object>) mock(LockingTaskExecutor.TaskResult.class);

        when(taskResult.wasExecuted()).thenReturn(true);

        when(executor.executeWithLock(
                any(LockingTaskExecutor.TaskWithResult.class),
                any(LockConfiguration.class)
        )).thenAnswer(invocation -> {
            LockingTaskExecutor.TaskWithResult<?> task =
                    invocation.getArgument(0);
            Object result = task.call();

            when(taskResult.getResult()).thenReturn(result);
            return taskResult;
        });

        when(productRepositoryJPA.findById(productId)).thenReturn(java.util.Optional.of(product));

        var result = productService.reserveProduct(reservation);

        assertThat(result).isLeft();
        assertThat(result).hasLeftValueSatisfying(error -> {
            org.assertj.core.api.Assertions.assertThat(error.message())
                    .isEqualTo("INSUFFICIENT_STOCK");
        });
    }

    @Test
    void shouldReserveProductSuccessfully() throws Throwable {
        UUID productId = UUID.randomUUID();
        ProductReservation reservation = new ProductReservation(productId, 3);

        Product product = new Product(productId, "Laptop", 10);
        Product savedProduct = new Product(productId, "Laptop", 7);

        @SuppressWarnings("unchecked")
        LockingTaskExecutor.TaskResult<Object> taskResult =
                (LockingTaskExecutor.TaskResult<Object>) mock(LockingTaskExecutor.TaskResult.class);

        when(taskResult.wasExecuted()).thenReturn(true);

        when(executor.executeWithLock(
                any(LockingTaskExecutor.TaskWithResult.class),
                any(LockConfiguration.class)
        )).thenAnswer(invocation -> {
            LockingTaskExecutor.TaskWithResult<?> task =
                    invocation.getArgument(0);
            Object result = task.call();

            when(taskResult.getResult()).thenReturn(result);
            return taskResult;
        });

        when(productRepositoryJPA.findById(productId)).thenReturn(java.util.Optional.of(product));
        when(productRepositoryJPA.save(any(Product.class))).thenReturn(savedProduct);

        var result = productService.reserveProduct(reservation);

        assertThat(result).isRight();
        assertThat(result.get().getStock()).isEqualTo(7);
    }
}