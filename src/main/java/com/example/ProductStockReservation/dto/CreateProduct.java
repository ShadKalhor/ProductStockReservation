package com.example.ProductStockReservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProduct (@NotNull String name,@PositiveOrZero int stock){
}
