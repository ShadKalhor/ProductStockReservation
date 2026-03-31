package com.example.ProductStockReservation.Service;

import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.ErrorType;
import com.example.ProductStockReservation.Exception.StructuredError;
import com.example.ProductStockReservation.Repository.ProductRepositoryJPA;
import com.example.ProductStockReservation.dto.ProductReservation;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepositoryJPA productRepo;
    private final LockingTaskExecutor executor;



    public Either<StructuredError,Product> reserveProduct(ProductReservation requestedProduct) {

        LockConfiguration lockConfig = new LockConfiguration(
                Instant.now(),
                "lock_product_" + requestedProduct.id(),
                Duration.ofMinutes(1),
                Duration.ZERO
        );


        return Try.of(() ->
                        executor.executeWithLock(() -> {
                            Option<Product> productOption = productRepo.findOptionById(requestedProduct.id());

                            return productOption
                                    .toEither(new StructuredError(
                                            "Could Not Find Product",
                                            ErrorType.NOT_FOUND_ERROR
                                    ))
                                    .flatMap(product -> {
                                        if (product.getStock() < requestedProduct.quantity()) {
                                            return Either.left(new StructuredError(
                                                    "INSUFFICIENT_STOCK",
                                                    ErrorType.SERVER_ERROR
                                            ));
                                        }

                                        Try.run(() -> Thread.sleep(10000))
                                                .onFailure(ex -> Thread.currentThread().interrupt())
                                                .get();

                                        product.setStock(product.getStock() - requestedProduct.quantity());
                                        return Either.right(productRepo.save(product));
                                    });
                        }, lockConfig)
                )
                .toEither()
                .mapLeft(ex -> new StructuredError(
                        "Internal Server Error Reserving Product",
                        ErrorType.SERVER_ERROR
                ))
                .flatMap(taskResult -> {
                    if (!taskResult.wasExecuted()) {
                        return Either.left(new StructuredError(
                                "LOCK_NOT_ACQUIRED",
                                ErrorType.SERVER_ERROR
                        ));
                    }

                    Either<StructuredError, Product> result = taskResult.getResult();

                    if (result == null) {
                        return Either.left(new StructuredError(
                                "LOCK_EXECUTED_BUT_RESULT_NULL",
                                ErrorType.SERVER_ERROR
                        ));
                    }

                    return result;
                });

    }







    public Either<StructuredError, Page<Product>> findAllPageable(Pageable pageable) {
        return Try.of(() -> productRepo.findAll(pageable)).toEither().mapLeft(throwable -> new StructuredError("Error Loading Data",ErrorType.SERVER_ERROR));
    }

    public Either<StructuredError, Product> save(@Valid Product product) {

        return Try.of(() -> productRepo.save(product)).toEither().mapLeft(throwable -> new StructuredError("Issue when Saving The Product", ErrorType.SERVER_ERROR));


    }
}
