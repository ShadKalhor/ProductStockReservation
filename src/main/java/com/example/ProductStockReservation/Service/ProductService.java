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
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepositoryJPA productRepo;
    private final LockingTaskExecutor executor;



    public Either<StructuredError,Product> reserveProduct(ProductReservation requestedProduct) {


        log.debug("Preparing Lock Configuration for ProductReservation. ProductId: {}, Quantity: {}",
                requestedProduct.id(),
                requestedProduct.quantity());

        LockConfiguration lockConfig = new LockConfiguration(
                Instant.now(),
                "lock_product_" + requestedProduct.id(),
                Duration.ofMinutes(1),
                Duration.ZERO
        );


        return Try.of(() ->
                        executor.executeWithLock(() -> {
                            log.debug("Reserve Product Started. ProductId: {}, Quantity: {}.",
                                    requestedProduct.id(),
                                    requestedProduct.quantity());

                            log.debug("Looking Up Product in Database. ProductId: {}", requestedProduct.id());
                            Option<Product> productOption = productRepo.findOptionById(requestedProduct.id());

                            return productOption
                                    .toEither(new StructuredError(
                                            "Could Not Find Product",
                                            ErrorType.NOT_FOUND_ERROR
                                    ))
                                    .peekLeft(error -> log.warn("Product Not Found. ProductId: {}", requestedProduct.id()))
                                    .flatMap(product -> {
                                        if (product.getStock() < requestedProduct.quantity()) {
                                            log.warn("Insufficient Stock. ProductStock: {}, RequestedStock: {}",
                                                    product.getStock(), requestedProduct.quantity());

                                            return Either.left(new StructuredError(
                                                    "INSUFFICIENT_STOCK",
                                                    ErrorType.SERVER_ERROR
                                            ));
                                        }
                                        log.debug("Found Product With Sufficient Stock Successfully With Id: {}", requestedProduct.id());

                                        Try.run(() -> Thread.sleep(10000))
                                                .onFailure(ex -> Thread.currentThread().interrupt())
                                                .get();

                                        product.setStock(product.getStock() - requestedProduct.quantity());
                                        log.debug("Saving Updated Product and Returning It's Value.");
                                        return Either.right(productRepo.save(product));
                                    });
                        }, lockConfig)
                )
                .toEither()
                .mapLeft(ex -> new StructuredError(
                        "Internal Server Error Reserving Product",
                        ErrorType.SERVER_ERROR
                )).peekLeft(error -> log.warn("Internal Server Error Reserving Product. ProductId: {}", requestedProduct.id()))
                .flatMap(taskResult -> {
                    if (!taskResult.wasExecuted()) {
                        log.warn("Lock Not Acquired. ProductId: {}", requestedProduct.id());

                        return Either.left(new StructuredError(
                                "LOCK_NOT_ACQUIRED",
                                ErrorType.SERVER_ERROR
                        ));
                    }

                    Either<StructuredError, Product> result = taskResult.getResult();

                    if (result == null) {
                        log.warn("Lock Executed But Result is Null. ProductId: {}",
                                requestedProduct.id());

                        return Either.left(new StructuredError(
                                "LOCK_EXECUTED_BUT_RESULT_NULL",
                                ErrorType.SERVER_ERROR
                        ));
                    }

                    return result;
                });

    }







    public Either<StructuredError, Page<Product>> findAllPageable(Pageable pageable) {
        log.debug("Looking Up Database For List of Products.");
        return Try.of(() -> productRepo.findAll(pageable)).toEither().mapLeft(throwable -> new StructuredError("Error Loading Data",ErrorType.SERVER_ERROR));
    }

    public Either<StructuredError, Product> save(@Valid Product product) {

        log.debug("Saving Product. ProductName: {}, ProductStock: {}",product.getName(),product.getStock());
        return Try.of(() -> productRepo.save(product)).toEither().mapLeft(throwable -> new StructuredError("Issue when Saving The Product", ErrorType.SERVER_ERROR));


    }
}
