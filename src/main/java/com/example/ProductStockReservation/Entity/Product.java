package com.example.ProductStockReservation.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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

    private UUID id;

    private String name;
    private int stock;
    private long version;



    @PrePersist
    public void prePersist(){
        if (id == null) id = UUID.randomUUID();
    }

}
