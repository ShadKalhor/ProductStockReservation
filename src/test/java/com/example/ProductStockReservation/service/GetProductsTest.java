package com.example.ProductStockReservation.service;


import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.ErrorStructureException;
import com.example.ProductStockReservation.Exception.StructuredError;
import com.example.ProductStockReservation.Repository.ProductRepositoryJPA;
import com.example.ProductStockReservation.Service.ProductService;
import io.vavr.control.Either;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith({SoftAssertionsExtension.class, MockitoExtension.class})
public class GetProductsTest {

    @Mock
    private ProductRepositoryJPA productRepositoryJPA;

    @InjectMocks
    private ProductService productService;


    @Test
    void shouldReturnProducts(SoftAssertions softly){

        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(0,5,sort);


        Product product1 = new Product(UUID.randomUUID(),"Laptop1",20);
        Product product2 = new Product(UUID.randomUUID(),"Laptop2",14);
        Product product3 = new Product(UUID.randomUUID(),"Laptop3",18);

        List<Product> mockProducts = List.of(product1, product2, product3);

        Page<Product> mockPage = new PageImpl<>(mockProducts);


        when(productRepositoryJPA.findAll(pageable)).thenReturn(mockPage);

        Either<StructuredError, Page<Product>> resultContained = productService.findAllPageable(pageable);
        Page<Product> result = resultContained.getOrElseThrow(ErrorStructureException::new);
        softly.assertThat(result)
                .satisfiesExactlyInAnyOrder(
                        itemOutput -> softly.assertThat(itemOutput).isEqualTo(product1),
                        itemOutput -> softly.assertThat(itemOutput).isEqualTo(product2),
                        itemOutput -> softly.assertThat(itemOutput).isEqualTo(product3)
                );
        verify(productRepositoryJPA, times(1)).findAll(pageable);

    }


}
