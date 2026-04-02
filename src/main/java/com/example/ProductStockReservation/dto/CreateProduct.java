package com.example.ProductStockReservation.dto;

import jakarta.validation.constraints.NotNull;

public record CreateProduct (@NotNull String name,@NotNull int stock){
}
