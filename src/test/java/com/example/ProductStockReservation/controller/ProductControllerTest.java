package com.example.ProductStockReservation.controller;

import com.example.ProductStockReservation.Controller.ProductController;
import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.GlobalExceptionHandler;
import com.example.ProductStockReservation.Exception.StructuredError;
import com.example.ProductStockReservation.Exception.ErrorType;
import com.example.ProductStockReservation.Service.ProductService;
import com.example.ProductStockReservation.dto.CreateProduct;
import com.example.ProductStockReservation.dto.ProductReservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {
        Product savedProduct = new Product(UUID.randomUUID(), "Laptop", 10);
        CreateProduct request = new CreateProduct("Laptop", 10);

        when(productService.save(any(Product.class))).thenReturn(Either.right(savedProduct));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedProduct.getId().toString()))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.stock").value(10));

        verify(productService).save(any(Product.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateProductRequestIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "name": null,
                  "stock": -1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("INVALID_REQUEST"));
    }

    @Test
    void shouldReturnProductsPage() throws Exception {
        Product product1 = new Product(UUID.randomUUID(), "Laptop1", 20);
        Product product2 = new Product(UUID.randomUUID(), "Laptop2", 10);

        var pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        var page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(productService.findAllPageable(eq(pageable))).thenReturn(Either.right(page));

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("ascending", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(product1.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value("Laptop1"))
                .andExpect(jsonPath("$.content[0].stock").value(20))
                .andExpect(jsonPath("$.content[1].id").value(product2.getId().toString()))
                .andExpect(jsonPath("$.content[1].name").value("Laptop2"))
                .andExpect(jsonPath("$.content[1].stock").value(10));
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("sortBy", "price"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Sort Field."));
    }

    @Test
    void shouldReserveProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductReservation request = new ProductReservation(productId, 2);
        Product reservedProduct = new Product(productId, "Laptop", 8);

        when(productService.reserveProduct(any(ProductReservation.class))).thenReturn(Either.right(reservedProduct));

        mockMvc.perform(post("/api/products/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.stock").value(8));
    }

    @Test
    void shouldReturnBadRequestWhenReserveProductRequestIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "id": null,
                  "quantity": -2
                }
                """;

        mockMvc.perform(post("/api/products/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("INVALID_REQUEST"));
    }

    @Test
    void shouldMapServiceErrorToHttpStatusWhenCreatingProduct() throws Exception {
        CreateProduct request = new CreateProduct("Laptop", 10);

        when(productService.save(any(Product.class))).thenReturn(
                Either.left(new StructuredError("Issue when Saving The Product", ErrorType.SERVER_ERROR))
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Issue when Saving The Product"));
    }
}

/*

package com.example.ProductStockReservation.controller;

import com.example.ProductStockReservation.Controller.ProductController;
import com.example.ProductStockReservation.Entity.Product;
import com.example.ProductStockReservation.Exception.ErrorType;
import com.example.ProductStockReservation.Exception.GlobalExceptionHandler;
import com.example.ProductStockReservation.Exception.StructuredError;
import com.example.ProductStockReservation.Service.ProductService;
import com.example.ProductStockReservation.dto.CreateProduct;
import com.example.ProductStockReservation.dto.ProductReservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = Mockito.mock(ProductService.class);
        objectMapper = new ObjectMapper();

        ProductController productController = new ProductController(productService);

        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        Product savedProduct = new Product(UUID.randomUUID(), "Laptop", 10);
        CreateProduct request = new CreateProduct("Laptop", 10);

        when(productService.save(any(Product.class))).thenReturn(Either.right(savedProduct));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedProduct.getId().toString()))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.stock").value(10));

        verify(productService).save(any(Product.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateProductRequestIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "name": null,
                  "stock": -1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnProductsPage() throws Exception {
        Product product1 = new Product(UUID.randomUUID(), "Laptop1", 20);
        Product product2 = new Product(UUID.randomUUID(), "Laptop2", 10);

        var pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        var page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(productService.findAllPageable(eq(pageable))).thenReturn(Either.right(page));

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("ascending", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(product1.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value("Laptop1"))
                .andExpect(jsonPath("$.content[0].stock").value(20))
                .andExpect(jsonPath("$.content[1].id").value(product2.getId().toString()))
                .andExpect(jsonPath("$.content[1].name").value("Laptop2"))
                .andExpect(jsonPath("$.content[1].stock").value(10));
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("sortBy", "price"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Sort Field."));
    }

    @Test
    void shouldReserveProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductReservation request = new ProductReservation(productId, 2);
        Product reservedProduct = new Product(productId, "Laptop", 8);

        when(productService.reserveProduct(any(ProductReservation.class))).thenReturn(Either.right(reservedProduct));

        mockMvc.perform(post("/api/products/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.stock").value(8));
    }

    @Test
    void shouldReturnBadRequestWhenReserveProductRequestIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "id": null,
                  "quantity": -2
                }
                """;

        mockMvc.perform(post("/api/products/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldMapServiceErrorToHttpStatusWhenCreatingProduct() throws Exception {
        CreateProduct request = new CreateProduct("Laptop", 10);

        when(productService.save(any(Product.class))).thenReturn(
                Either.left(new StructuredError("Issue when Saving The Product", ErrorType.SERVER_ERROR))
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Issue when Saving The Product"));
    }
}*/
