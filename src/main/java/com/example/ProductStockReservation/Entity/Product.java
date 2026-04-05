package com.example.ProductStockReservation.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "products")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private UUID id;

    @NotNull
    private String name;

    @PositiveOrZero
    private int stock;



    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
    }

}
